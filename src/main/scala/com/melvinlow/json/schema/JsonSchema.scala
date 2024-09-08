package com.melvinlow.json.schema

import io.circe.Json

object JsonSchema {
  inline def apply[T: JsonSchemaEncoder]: Json = JsonSchemaEncoder[T].schema

  inline def apply[T: JsonSchemaEncoder](inline dummy: T): Json = apply[T]
}
