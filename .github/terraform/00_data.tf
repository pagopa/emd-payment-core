# KV Core
data "azurerm_key_vault" "key_vault_core" {
  name                = "${var.prefix}-${var.env_short}-${var.location_short}-core-kv"
  resource_group_name = "${var.prefix}-${var.env_short}-${var.location_short}-core-sec-rg"
}

# Kv Domain
data "azurerm_key_vault" "key_vault_domain" {
  name                = "${var.prefix}-${var.env_short}-${var.location_short}-${var.domain}-kv"
  resource_group_name = "${var.prefix}-${var.env_short}-${var.location_short}-${var.domain}-sec-rg"
}

# Github
data "github_organization_teams" "all" {
  root_teams_only = true
  summary_only    = true
}
