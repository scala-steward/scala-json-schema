# Scala Json Schema (Lite)

This is a minimal library to generate JSON schema from Scala ADTs.
It is built on top of [circe](https://circe.github.io/circe/).

To reduce complexity and maintenance burden, this library
does the absolute minimum: It will traverse your ADT and generate
a schema with nested types and properties. No other fields are included.

Product types get the type `object` and sum types are converted into `anyOf`.

You can inject all other fields via annotation.
As intended, there is zero validation--you can do whatever you want.

## Examples:

Encoding a product:

```scala mdoc
import com.melvinlow.json.schema.*
import com.melvinlow.json.schema.generic.auto.given
import com.melvinlow.json.schema.instances.given

case class Foo(x: Int, y: String)

JsonSchema[Foo].spaces2
```

Encoding a coproduct:

```scala mdoc
enum Bar:
  case A, B

JsonSchema[Bar].spaces2
```

Adding fields via annotation (it takes as input any `String` and `Json` key-value pair):

```scala mdoc
import com.melvinlow.json.schema.annotation.JsonSchemaField
import io.circe.Json
import io.circe.syntax.*

@JsonSchemaField("title", "dog".asJson)
@JsonSchemaField("required", Array("name").asJson)
case class Dog(
  @JsonSchemaField("minLength", 1.asJson)
  name: String
)

JsonSchema[Dog].spaces2
```

Creating an encoder for a new type:

```scala mdoc
object H:
  @JsonSchemaField("description", "my custom int".asJson)
  opaque type MyInt = Int

given JsonSchemaEncoder[H.MyInt] with
  def schema: Json =
    val base = Json.obj("type" -> "integer".asJson)
    val annotations = JsonSchemaField.onType[H.MyInt]
    base.deepMerge(Json.obj(annotations*))

JsonSchema[H.MyInt].spaces2
```
