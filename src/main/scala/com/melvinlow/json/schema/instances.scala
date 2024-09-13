package com.melvinlow.json.schema

import io.circe.Json

trait instances_low_priority {
  given intJsonSchemaInstance[T <: Int]: JsonSchemaEncoder[T] with {
    def schema: Json = Json.obj("type" -> Json.fromString("integer"))
  }

  given stringJsonSchemaInstance[T <: String]: JsonSchemaEncoder[T] with {
    def schema: Json = Json.obj("type" -> Json.fromString("string"))
  }

  given longJsonSchemaInstance[T <: Long]: JsonSchemaEncoder[T] with {
    def schema: Json = Json.obj("type" -> Json.fromString("integer"))
  }

  given doubleJsonSchemaInstance[T <: Double]: JsonSchemaEncoder[T] with {
    def schema: Json = Json.obj("type" -> Json.fromString("number"))
  }

  given floatJsonSchemaEncoder[T <: Float]: JsonSchemaEncoder[T] with {
    def schema: Json = Json.obj("type" -> Json.fromString("number"))
  }

  given booleanJsonSchemaEncoder[T <: Boolean]: JsonSchemaEncoder[T] with {
    def schema: Json = Json.obj("type" -> Json.fromString("boolean"))
  }

  given listJsonSchemaEncoder[T: JsonSchemaEncoder]: JsonSchemaEncoder[List[T]]
  with {
    def schema: Json =
      Json
        .obj(
          "type"  -> Json.fromString("array"),
          "items" -> JsonSchemaEncoder[T].schema
        )
  }

  given arrayJsonSchemaEncoder[T: JsonSchemaEncoder]
    : JsonSchemaEncoder[Array[T]]
  with {
    def schema: Json =
      Json
        .obj(
          "type"  -> Json.fromString("array"),
          "items" -> JsonSchemaEncoder[T].schema
        )
  }
}

trait instances extends instances_low_priority {
  given nullJsonSchemaEncoder: JsonSchemaEncoder[Null] with {
    def schema: Json = Json.obj("type" -> Json.fromString("null"))
  }
}

object instances extends instances
