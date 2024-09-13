package com.melvinlow.json.schema

import io.circe.Json

import com.melvinlow.json.schema.Resources.*
import com.melvinlow.json.schema.annotation.JsonSchemaField
import com.melvinlow.json.schema.generic.auto.given
import com.melvinlow.json.schema.syntax.*

class JsonSchemaEncoderSuite extends munit.FunSuite {
  test("encode a string") {
    val obtained = JsonSchema[String]
    val expected = Json.obj("type" -> Json.fromString("string"))

    assertEquals(obtained, expected)
  }

  test("encode a literal string") {
    val obtained = JsonSchema["hello"]
    val expected = Json.obj("type" -> Json.fromString("string"))

    assertEquals(obtained, expected)
  }

  test("encode a boolean") {
    val obtained = JsonSchema[Boolean]
    val expected = Json.obj("type" -> Json.fromString("boolean"))

    assertEquals(obtained, expected)
  }

  test("encode a literal boolean") {
    val obtained = JsonSchema[true]
    val expected = Json.obj("type" -> Json.fromString("boolean"))

    assertEquals(obtained, expected)
  }

  test("encode an integer") {
    val obtained = JsonSchema[Int]
    val expected = Json.obj("type" -> Json.fromString("integer"))

    assertEquals(obtained, expected)
  }

  test("encode a literal integer") {
    val obtained = JsonSchema[5]
    val expected = Json.obj("type" -> Json.fromString("integer"))

    assertEquals(obtained, expected)
  }

  test("encode a long") {
    val obtained = JsonSchema[Long]
    val expected = Json.obj("type" -> Json.fromString("integer"))

    assertEquals(obtained, expected)
  }

  test("encode a literal long") {
    val obtained = JsonSchema[5L]
    val expected = Json.obj("type" -> Json.fromString("integer"))

    assertEquals(obtained, expected)
  }

  test("encode a double") {
    val obtained = JsonSchema[Double]
    val expected = Json.obj("type" -> Json.fromString("number"))

    assertEquals(obtained, expected)
  }

  test("encode a literal double") {
    val obtained = JsonSchema[5.0]
    val expected = Json.obj("type" -> Json.fromString("number"))

    assertEquals(obtained, expected)
  }

  test("encode a float") {
    val obtained = JsonSchema[Float]
    val expected = Json.obj("type" -> Json.fromString("number"))

    assertEquals(obtained, expected)
  }

  test("encode a literal float") {
    val obtained = JsonSchema[5.0f]
    val expected = Json.obj("type" -> Json.fromString("number"))

    assertEquals(obtained, expected)
  }

  test("encode null") {
    val obtained = JsonSchema[Null](null)
    val expected = Json.obj("type" -> Json.fromString("null"))

    assertEquals(obtained, expected)
  }

  test("encode a list of strings") {
    val obtained = JsonSchema[List[String]]
    val expected = Json.obj(
      "type"  -> Json.fromString("array"),
      "items" -> Json.obj("type" -> Json.fromString("string"))
    )

    assertEquals(obtained, expected)
  }

  test("encode an array of integers") {
    val obtained = JsonSchema[Array[Int]]
    val expected = Json.obj(
      "type"  -> Json.fromString("array"),
      "items" -> Json.obj("type" -> Json.fromString("integer"))
    )

    assertEquals(obtained, expected)
  }

  test("encode a new type with an custom field") {
    val obtained = JsonSchema[OpaqueType]
    val expected = Json.obj(
      "type"        -> Json.fromString("integer"),
      "description" -> Json.fromString("A custom description"),
      "title"       -> Json.fromString("A custom title")
    )

    assertEquals(obtained, expected)
  }

  test("encode with various syntax") {
    case class A(name: String)
    val x = A("hello")

    val obtained1 = JsonSchema[A]
    val obtained2 = JsonSchema(x)
    val obtained3 = x.jsonSchema
    val obtained4 = JsonSchemaEncoder[A].schema
    val obtained5 = JsonSchemaEncoder.ev[A].schema

    val expected = Json.obj(
      "type" -> Json.fromString("object"),
      "properties" -> Json.obj(
        "name" -> Json.obj("type" -> Json.fromString("string"))
      )
    )

    assertEquals(obtained1, expected)
    assertEquals(obtained2, expected)
    assertEquals(obtained3, expected)
    assertEquals(obtained4, expected)
    assertEquals(obtained5, expected)
  }

  test("encode a product type") {
    case class A(name: String, age: Int)

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "type" -> Json.fromString("object"),
      "properties" -> Json.obj(
        "name" -> Json.obj("type" -> Json.fromString("string")),
        "age"  -> Json.obj("type" -> Json.fromString("integer"))
      )
    )

    assertEquals(obtained, expected)
  }

  test("encode a sum type") {
    sealed trait A
    case object B extends A
    case object C extends A

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "anyOf" -> Json.arr(
        Json
          .obj("type" -> Json.fromString("object"), "properties" -> Json.obj()),
        Json.obj(
          "type"       -> Json.fromString("object"),
          "properties" -> Json.obj()
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("encode a nested product") {
    type A = ("hello", (3, true))

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "type" -> Json.fromString("object"),
      "properties" -> Json.obj(
        "_1" -> Json.obj("type" -> Json.fromString("string")),
        "_2" -> Json.obj(
          "type" -> Json.fromString("object"),
          "properties" -> Json.obj(
            "_1" -> Json.obj("type" -> Json.fromString("integer")),
            "_2" -> Json.obj("type" -> Json.fromString("boolean"))
          )
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("encode a nested sum") {
    sealed trait A
    case object X  extends A
    sealed trait B extends A
    case object Y  extends B
    case object Z  extends B

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "anyOf" -> Json.arr(
        Json
          .obj("type" -> Json.fromString("object"), "properties" -> Json.obj()),
        Json.obj(
          "anyOf" -> Json.arr(
            Json.obj(
              "type"       -> Json.fromString("object"),
              "properties" -> Json.obj()
            ),
            Json.obj(
              "type"       -> Json.fromString("object"),
              "properties" -> Json.obj()
            )
          )
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("encode an oldschool ADT") {
    sealed abstract class A
    case object B              extends A
    final case class C(x: Int) extends A

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "anyOf" -> Json.arr(
        Json
          .obj("type" -> Json.fromString("object"), "properties" -> Json.obj()),
        Json.obj(
          "type" -> Json.fromString("object"),
          "properties" -> Json.obj(
            "x" -> Json.obj("type" -> Json.fromString("integer"))
          )
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("encode a newschool ADT") {
    enum A {
      case B
      case C(x: Int)
    }

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "anyOf" -> Json.arr(
        Json
          .obj("type" -> Json.fromString("object"), "properties" -> Json.obj()),
        Json.obj(
          "type" -> Json.fromString("object"),
          "properties" -> Json.obj(
            "x" -> Json.obj("type" -> Json.fromString("integer"))
          )
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("encode with constructor custom field") {
    case class A(
      @JsonSchemaField(
        key = "description",
        value = Json.fromString("name")
      ) name: String,
      @JsonSchemaField(
        key = "description",
        value = Json.fromString("age")
      ) age: Int
    )

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "type" -> Json.fromString("object"),
      "properties" -> Json.obj(
        "name" -> Json.obj(
          "type"        -> Json.fromString("string"),
          "description" -> Json.fromString("name")
        ),
        "age" -> Json.obj(
          "type"        -> Json.fromString("integer"),
          "description" -> Json.fromString("age")
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("encode with multiple constructor custom fields") {
    case class A(
      @JsonSchemaField(
        key = "description",
        value = Json.fromString("name")
      )

      @JsonSchemaField(
        key = "required",
        value = Json.fromBoolean(true)
      )
      val name: String
    )

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "type" -> Json.fromString("object"),
      "properties" -> Json.obj(
        "name" -> Json.obj(
          "type"        -> Json.fromString("string"),
          "description" -> Json.fromString("name"),
          "required"    -> Json.fromBoolean(true)
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("encode with child custom field") {
    sealed trait A
    @JsonSchemaField(
      key = "description",
      value = Json.fromString("B")
    )
    case object B extends A

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "anyOf" -> Json.arr(
        Json.obj(
          "type"        -> Json.fromString("object"),
          "properties"  -> Json.obj(),
          "description" -> Json.fromString("B")
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("encode with multiple child custom fields") {
    sealed trait A
    @JsonSchemaField(
      key = "description",
      value = Json.fromString("B")
    )
    @JsonSchemaField(
      key = "required",
      value = Json.fromBoolean(true)
    )
    case object B extends A

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "anyOf" -> Json.arr(
        Json.obj(
          "type"        -> Json.fromString("object"),
          "properties"  -> Json.obj(),
          "description" -> Json.fromString("B"),
          "required"    -> Json.fromBoolean(true)
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("encode with nested custom fields") {
    enum TestEnum {
      case A

      @JsonSchemaField(
        key = "description",
        value = Json.fromString("B")
      ) case B

      case C(
        @JsonSchemaField(
          key = "description",
          value = Json.fromString("x")
        ) x: Int
      )
    }

    val obtained = JsonSchema[TestEnum]
    val expected = Json.obj(
      "anyOf" -> Json.arr(
        Json
          .obj("type" -> Json.fromString("object"), "properties" -> Json.obj()),
        Json.obj(
          "type"        -> Json.fromString("object"),
          "properties"  -> Json.obj(),
          "description" -> Json.fromString("B")
        ),
        Json.obj(
          "type" -> Json.fromString("object"),
          "properties" -> Json.obj(
            "x" -> Json.obj(
              "type"        -> Json.fromString("integer"),
              "description" -> Json.fromString("x")
            )
          )
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("must handle alias types") {
    case class A(name: String)
    type AAlias          = A
    type OpaqueTypeAlias = OpaqueType

    case class T(a: AAlias, b: OpaqueTypeAlias)

    val obtained = JsonSchema[T]
    val expected = Json.obj(
      "type" -> Json.fromString("object"),
      "properties" -> Json.obj(
        "a" -> Json.obj(
          "type" -> Json.fromString("object"),
          "properties" -> Json.obj(
            "name" -> Json.obj("type" -> Json.fromString("string"))
          )
        ),
        "b" -> Json.obj(
          "type"        -> Json.fromString("integer"),
          "description" -> Json.fromString("A custom description"),
          "title"       -> Json.fromString("A custom title")
        )
      )
    )

    assertEquals(obtained, expected)
  }

  test("must handle alias type 2") {
    type A = Int
    @JsonSchemaField(
      key = "K",
      value = Json.fromString("V")
    )
    type B = A
    type C = B

    val obtained = JsonSchema[C]
    val expected = Json.obj(
      "type" -> Json.fromString("integer")
    )

    assertEquals(obtained, expected)
  }

  test("must override type field") {
    @JsonSchemaField(
      key = "type",
      value = Json.fromString("string")
    )
    case class A(x: Int)

    val obtained = JsonSchema[A]

    // invalid schema, but just for testing
    val expected = Json.obj(
      "type" -> Json.fromString("string"),
      "properties" -> Json.obj(
        "x" -> Json.obj("type" -> Json.fromString("integer"))
      )
    )

    assertEquals(obtained, expected)
  }

  test("must override constructor field") {
    case class A(
      @JsonSchemaField(
        key = "type",
        value = Json.fromString("string")
      ) x: Int
    )

    val obtained = JsonSchema[A]
    val expected = Json.obj(
      "type" -> Json.fromString("object"),
      "properties" -> Json.obj(
        "x" -> Json.obj("type" -> Json.fromString("string"))
      )
    )

    assertEquals(obtained, expected)
  }

  test("must override child field") {
    sealed abstract class A
    @JsonSchemaField(
      key = "type",
      value = Json.fromString("string")
    )
    case object B extends A

    val obtained = JsonSchema[A]

    // invalid schema, but just for testing
    val expected = Json.obj(
      "anyOf" -> Json.arr(
        Json.obj(
          "type"       -> Json.fromString("string"),
          "properties" -> Json.obj()
        )
      )
    )

    assertEquals(obtained, expected)
  }
}
