package com.melvinlow.json.schema.syntax

import io.circe.Json

import com.melvinlow.json.schema.JsonSchemaEncoder

extension [T: JsonSchemaEncoder](inline dummy: T) {
  inline def jsonSchema: Json = JsonSchemaEncoder[T].schema
}
