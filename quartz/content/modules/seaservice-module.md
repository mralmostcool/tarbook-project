---
title: "SeaService Bounded Context Module"
---

# SeaService Bounded Context Module

The SeaService Bounded Context manages voyage logging, watchkeeping hours calculation, and Master statutory discharge endorsements.

## Managed Domain Entities

- [[database/tables/sea_service_records|sea_service_records]]: Shipboard voyage logs with PostGIS `btree_gist` non-overlapping temporal exclusion constraint.
- [[database/tables/sea_service_endorsements|sea_service_endorsements]]: Statutory Master and Chief Engineer discharge endorsements.

- [[database/postgis-spatial|PostGIS Spatial Extensions & GiST Indexing]]
- [[database/tables/index|Database Table Descriptors Registry]]
