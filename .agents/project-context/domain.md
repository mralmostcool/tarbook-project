# Domain Model

## Core Concepts

<!-- List important domain concepts. -->

## Terminology

<!-- Canonical terminology used throughout the system. -->

### Officer Signing Key

A hardware-backed ECDSA P-256 key used exclusively for statutory record signing, isolated from API authentication.

### Canonical Payload

A deterministic byte-level encoding format for data before signing, ensuring verification consistency.

### User

A human identity that can authenticate.

### Organization

A tenant within the platform.

### Membership

The relationship between a User and an Organization.

### Role

A set of permissions assigned through a Membership.

## Entities

<!-- Major entities and their responsibilities. -->

## Relationships

<!-- Important relationships between entities. -->

## Invariants

<!-- Rules that must always be true. -->

## State Machines

<!-- Important lifecycle/state transitions. -->

## Naming Rules

<!-- Domain-specific naming conventions. -->