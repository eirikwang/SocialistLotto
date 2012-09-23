package org.clarity.demo.cqrs.domain


trait Versionable {
  def id: Long
  def version: Long
}
