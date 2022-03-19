# Notes on this fork

This fork exists to fix an incompatibility issue between Ebean and H2 2.x. The fix exists on the branch `h2-compatibility-fix`. The only change
made is to the file [ScalarTypeClob](ebean-core/src/main/java/io/ebeaninternal/server/type/ScalarTypeClob.java) where `bind()` needed to be overridden
so that `setClob()` was properly called instead of `setString()`.

Also, ebean-agent has been added to this repository for its inclusion with this fork.

# Below is the original Ebean README

[![Build](https://github.com/ebean-orm/ebean/actions/workflows/build.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/build.yml)
[![Maven Central : ebean](https://maven-badges.herokuapp.com/maven-central/io.ebean/ebean/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.ebean/ebean)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/ebean-orm/ebean/blob/master/LICENSE)
[![Multi-JDK Build](https://github.com/ebean-orm/ebean/actions/workflows/multi-jdk-build.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/multi-jdk-build.yml)
[![JDK 18-ea](https://github.com/ebean-orm/ebean/actions/workflows/jdk-18-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/jdk-18-ea.yml)

[![H2Database](https://github.com/ebean-orm/ebean/actions/workflows/h2database.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/h2database.yml)
[![Postgres](https://github.com/ebean-orm/ebean/actions/workflows/postgres.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/postgres.yml)
[![MySql](https://github.com/ebean-orm/ebean/actions/workflows/mysql.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/mysql.yml)
[![MariaDB](https://github.com/ebean-orm/ebean/actions/workflows/mariadb.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/mariadb.yml)
[![SqlServer](https://github.com/ebean-orm/ebean/actions/workflows/sqlserver.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/sqlserver.yml)
[![Yugabyte](https://github.com/ebean-orm/ebean/actions/workflows/yugabyte.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/yugabyte.yml)

# Sponsors
<table>
  <tbody>
    <tr>
      <td align="center" valign="middle">
        <a href="https://www.foconis.de/" target="_blank">
          <img width="222px" src="https://www.foconis.de/templates/yootheme/cache/foconis_logo_322-709da1de.png">
        </a>
      </td>
      <td align="center" valign="middle">
        <a href="https://www.payintech.com/" target="_blank">
          <img width="222px" src="https://ebean.io/images/sponsor_PayinTech-logo-noir.png">
        </a>
      </td>
      <td align="center" valign="middle">
        <a href="https://www.premium-minds.com" target="_blank">
          <img width="222px" src="https://ebean.io/images/logo-med-principal.png">
        </a>
      </td>
      <td align="center" valign="middle">
        <a href="https://timerbee.de" target="_blank">
          <img width="222px" src="https://ebean.io/images/logo-timerbee.png">
        </a>
      </td>
    </tr>
  </tbody>
</table>

# Need help?
Post questions or issues to the Ebean google group - https://groups.google.com/forum/#!forum/ebean

# Documentation
Goto [https://ebean.io/docs/](https://ebean.io/docs/)


## Maven central links:
[Maven central - ebean](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22io.ebean%22%20AND%20a%3A%22ebean%22 "maven central ebean")

[Maven central - all related projects](http://search.maven.org/#search%7Cga%7C1%7Cebean "maven central all related projects")
