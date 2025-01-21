locals {
  # Common Tags:
  common_tags = {
    CreatedBy   = "Terraform"
    Environment = var.env
    Owner       = upper(var.prefix)
    Source      = "" # Repository URL
    CostCenter  = ""
  }

  # Repo
  github = {
    org        = "pagopa"
    repository = "" # Repository Name
  }

  env_secrets = {
    ENV_SECRET = "data.azurerm_key_vault_secret.CHANGE_ME.value"
  }
  env_variables = {
    ENV_VARIABLE = "ENV_VARIABLE"
  }

  repo_secrets = var.env_short == "p" ? {
    SECRET = "SECRET"
  } : {}
  repo_env = var.env_short == "p" ? {
    ENV_VARIABLE = "ENV_VARIABLE"
  } : {}

  map_repo = {
    "dev" : "*",
    "uat" : "uat"
    "prod" : "main"
  }
}
