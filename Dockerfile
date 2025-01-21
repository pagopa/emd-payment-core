# syntax=docker/dockerfile:1.4@sha256:9ba7531bd80fb0a858632727cf7a112fbfd19b17e94c4e84ced81e24ef1a0dbc

#
# 🎯 Version Management
#
ARG CORRETTO_VERSION="21-alpine3.20"
ARG CORRETTO_SHA="8b16834e7fabfc62d4c8faa22de5df97f99627f148058d52718054aaa4ea3674"
ARG GRADLE_VERSION="8.10.2"
ARG GRADLE_DOWNLOAD_SHA256="31c55713e40233a8303827ceb42ca48a47267a0ad4bab9177123121e71524c26"
ARG APPINSIGHTS_VERSION="3.6.2"

# 🌍 Timezone Configuration
ARG TZ="Europe/Rome"

# 🔧 Build Configuration
ARG GRADLE_OPTS="-Dorg.gradle.daemon=false \
    -Dorg.gradle.parallel=true \
    -Dorg.gradle.caching=true \
    -Dorg.gradle.configureondemand=true \
    -Dorg.gradle.jvmargs=-Xmx2g"

# 👤 App Configuration
ARG APP_USER="appuser"
ARG APP_GROUP="appgroup"
ARG APP_HOME="/app"
ARG GRADLE_HOME="/opt/gradle"

#
# 📥 Base Setup Stage
#
FROM amazoncorretto:${CORRETTO_VERSION}@sha256:${CORRETTO_SHA} AS base
ARG APP_USER
ARG APP_GROUP

# Install base packages
RUN apk add --no-cache \
    wget \
    unzip \
    bash \
    shadow

# Create Gradle user
RUN groupadd --system --gid 1000 ${APP_GROUP} && \
    useradd --system --gid ${APP_GROUP} --uid 1000 --shell /bin/bash --create-home ${APP_USER}

#
# 📦 Gradle Setup Stage
#
FROM base AS gradle-setup
ARG GRADLE_VERSION
ARG GRADLE_DOWNLOAD_SHA256
ARG GRADLE_HOME
ARG GRADLE_OPTS
ARG APP_USER
ARG APP_GROUP

# Set environment variables for Gradle
ENV GRADLE_OPTS="${GRADLE_OPTS}"
ENV GRADLE_HOME="${GRADLE_HOME}"
ENV PATH="${GRADLE_HOME}/bin:${PATH}"

WORKDIR /tmp

# Download and verify Gradle with progress bar
RUN echo "Downloading Gradle ${GRADLE_VERSION}..." && \
    wget --progress=bar:force --output-document=gradle.zip \
        "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" && \
    echo "Verifying download..." && \
    echo "${GRADLE_DOWNLOAD_SHA256}  gradle.zip" | sha256sum -c - && \
    echo "Installing Gradle..." && \
    unzip -q gradle.zip && \
    mv "gradle-${GRADLE_VERSION}" "${GRADLE_HOME}" && \
    ln -s "${GRADLE_HOME}/bin/gradle" /usr/bin/gradle && \
    rm gradle.zip && \
    # Setup Gradle user directories
    mkdir -p /home/${APP_USER}/.gradle && \
    chown --recursive ${APP_USER}:${APP_GROUP} /home/${APP_USER} && \
    # Verify installation
    echo "Verifying Gradle installation..." && \
    gradle --version

# Create Gradle volume
VOLUME /home/${APP_USER}/.gradle

#
# 📚 Dependencies Stage
#
FROM gradle-setup AS dependencies

WORKDIR /build

# Copy build configuration
COPY --chown=${APP_USER}:${APP_GROUP} build.gradle.kts settings.gradle.kts ./
COPY --chown=${APP_USER}:${APP_GROUP} gradle.lockfile ./
COPY --chown=${APP_USER}:${APP_GROUP} openapi openapi/

# Generate OpenAPI stubs and download dependencies
RUN mkdir -p src/main/java && \
    chown -R ${APP_USER}:${APP_GROUP} /build && \
    chmod -R 775 /build

USER ${APP_USER}

RUN gradle dependenciesBuild dependencies --no-daemon

#
# 🏗️ Build Stage
#
FROM dependencies AS build

# Copy source code
COPY --chown=${APP_USER}:${APP_GROUP} src src/

# Build application
RUN gradle bootJar --no-daemon

#
# 🚀 Runtime Stage
#
FROM amazoncorretto:${CORRETTO_VERSION}@sha256:${CORRETTO_SHA} AS runtime
ARG APP_USER
ARG APP_GROUP
ARG APP_HOME
ARG APPINSIGHTS_VERSION
ARG TZ

WORKDIR ${APP_HOME}

# Set timezone environment variable
ENV TZ=${TZ}

# 🛡️ Security Setup and Timezone
RUN apk upgrade --no-cache && \
    apk add --no-cache \
        tini \
        curl \
        # Configure timezone + ENV=TZ
        tzdata && \
    # Create user and group
    addgroup -S ${APP_GROUP} && \
    adduser -S ${APP_USER} -G ${APP_GROUP}

# 📦 Copy Artifacts
COPY --from=build /build/build/libs/*.jar ${APP_HOME}/app.jar
ADD --chmod=644 https://github.com/microsoft/ApplicationInsights-Java/releases/download/${APPINSIGHTS_VERSION}/applicationinsights-agent-${APPINSIGHTS_VERSION}.jar ${APP_HOME}/applicationinsights-agent.jar

# 📝 Set Permissions
RUN chown -R ${APP_USER}:${APP_GROUP} ${APP_HOME}

# 🔌 Container Configuration
EXPOSE 8080
USER ${APP_USER}

# 🎬 Startup Configuration
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-jar", "/app/app.jar"]
