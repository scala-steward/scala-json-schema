package com.melvinlow.json.schema.instances

import io.circe.Json

import com.melvinlow.json.schema.JsonSchemaEncoder

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

given nullJsonSchemaEncoder: JsonSchemaEncoder[Null] with {
  def schema: Json = Json.obj("type" -> Json.fromString("null"))
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

given arrayJsonSchemaEncoder[T: JsonSchemaEncoder]: JsonSchemaEncoder[Array[T]]
with {
  def schema: Json =
    Json
      .obj(
        "type"  -> Json.fromString("array"),
        "items" -> JsonSchemaEncoder[T].schema
      )
}
