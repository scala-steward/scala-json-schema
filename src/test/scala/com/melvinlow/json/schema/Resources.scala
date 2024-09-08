package com.melvinlow.json.schema

import io.circe.Json

import com.melvinlow.json.schema.annotation.JsonSchemaField

object Resources {
  @JsonSchemaField(
    key = "description",
    value = Json.fromString("A custom description")
  )
  @JsonSchemaField(
    key = "title",
    value = Json.fromString("A custom title")
  )
  opaque type OpaqueType = Int

  object OpaqueType {
    given JsonSchemaEncoder[OpaqueType] with {
      def schema: Json =
        Json
          .obj("type" -> Json.fromString("integer"))
          .deepMerge(Json.obj(JsonSchemaField.onType[OpaqueType]*))
    }
  }
}
