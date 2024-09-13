package com.melvinlow.json.schema.generic

import scala.annotation.nowarn
import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror

import io.circe.Json

import com.melvinlow.json.schema.annotation.JsonSchemaField
import com.melvinlow.json.schema.{JsonSchemaEncoder, instances}

trait auto {
  inline private def summonLabels[Elems <: Tuple]: List[String] =
    inline erasedValue[Elems] match {
      case _: (elem *: elems) =>
        constValue[elem].toString :: summonLabels[elems]
      case _: EmptyTuple => Nil
    }

  inline private def summonInstances[T, Elems <: Tuple]
    : List[JsonSchemaEncoder[?]] =
    inline erasedValue[Elems] match {
      case _: (elem *: elems) =>
        deriveOrSummon[T, elem] :: summonInstances[T, elems]
      case _: EmptyTuple => Nil
    }

  @nowarn
  inline private def deriveOrSummon[T, Elem]: JsonSchemaEncoder[Elem] =
    inline erasedValue[Elem] match {
      case _: T => deriveRec[T, Elem]
      case _    => summonInline[JsonSchemaEncoder[Elem]]
    }

  @nowarn
  inline private def deriveRec[T, Elem]: JsonSchemaEncoder[Elem] =
    inline erasedValue[T] match {
      case _: Elem =>
        error("infinite recursive derivation")
      case _ =>
        derived[Elem](using summonInline[Mirror.Of[Elem]])
    }

  private def sumEncoder[T: Mirror.SumOf](
    elems: => List[JsonSchemaEncoder[?]],
    elemLabels: => List[String],
    childAnnotations: => Map[String, List[(String, Json)]],
    typeAnnotations: => List[(String, Json)]
  ): JsonSchemaEncoder[T] = new JsonSchemaEncoder[T] {
    override def schema: Json = Json
      .obj(
        "anyOf" -> Json.arr(
          elems.zip(elemLabels).map { (elem, label) =>
            val annotations = childAnnotations.getOrElse(label, Nil)
            elem.schema.deepMerge(Json.obj(annotations*))
          }*
        )
      )
      .deepMerge(Json.obj(typeAnnotations*))
  }

  private def productEncoder[T: Mirror.ProductOf](
    elems: => List[JsonSchemaEncoder[?]],
    elemLabels: => List[String],
    constructorAnnotations: => Map[String, List[(String, Json)]],
    typeAnnotations: => List[(String, Json)]
  ): JsonSchemaEncoder[T] =
    new JsonSchemaEncoder[T] {
      override def schema = Json
        .obj(
          "type" -> Json.fromString("object"),
          "properties" -> Json.obj(
            elems
              .zip(elemLabels)
              .map { (elem, label) =>
                val annotations = constructorAnnotations.getOrElse(label, Nil)
                label -> elem.schema.deepMerge(Json.obj(annotations*))
              }*
          )
        )
        .deepMerge(Json.obj(typeAnnotations*))
    }

  inline def derived[T](using m: Mirror.Of[T]): JsonSchemaEncoder[T] = {
    lazy val elemInstances = summonInstances[T, m.MirroredElemTypes]
    lazy val elemLabels    = summonLabels[m.MirroredElemLabels]

    inline m match {
      case s: Mirror.SumOf[T] =>
        sumEncoder(
          elemInstances,
          elemLabels,
          childAnnotations = JsonSchemaField.onChildrenOf[T],
          typeAnnotations = JsonSchemaField.onType[T]
        )(using s)

      case p: Mirror.ProductOf[T] =>
        productEncoder(
          elemInstances,
          elemLabels,
          constructorAnnotations = JsonSchemaField.onConstructorParamsOf[T],
          typeAnnotations = JsonSchemaField.onType[T]
        )(using p)
    }
  }

  inline given derivedProduct[T: Mirror.ProductOf]: JsonSchemaEncoder[T] =
    derived[T]

  inline given derivedSum[T: Mirror.SumOf]: JsonSchemaEncoder[T] =
    derived[T]
}

object auto extends auto with semiauto with instances
