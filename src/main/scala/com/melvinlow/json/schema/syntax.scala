package com.melvinlow.json.schema

import io.circe.Json

object syntax {
  extension [T: JsonSchemaEncoder](inline dummy: T) {
    inline def jsonSchema: Json = JsonSchemaEncoder[T].schema
  }
}
