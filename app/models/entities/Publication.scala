package models.entities

import models.Backend
import models.gql.Fetchers.{diseasesFetcher, drugsFetcher, soTermsFetcher, targetsFetcher}
import models.gql.Objects.{diseaseImp, drugImp, targetImp}
import play.api.libs.json._
import sangria.schema.{Field, FloatType, ListType, LongType, ObjectType, OptionType, StringType, fields}
import slick.jdbc.GetResult

case class Publication(pmid: String, pmcid: Option[String], date: String, sentences: Option[JsValue])

object Publication {
  implicit val getSimilarityRowFromDB: GetResult[Publication] =
    GetResult(r => Publication(r.<<[Long].toString, r.<<?, r.<<,  r.<<?[String].map(Json.parse)))

  implicit val similarityImp: OFormat[Publication] = Json.format[Publication]

  val matchImp = ObjectType(
    "Match",
    fields[Backend, JsValue](
      Field("mappedId",
        StringType,
        description = None,
        resolve = js => (js.value \ "keywordId").as[String]),
      Field("matchedLabel",
        StringType,
        description = None,
        resolve = js => (js.value \ "label").as[String]),
      Field("sectionStart",
        OptionType(LongType),
        description = None,
        resolve = js => (js.value \ "sectionStart").asOpt[Long]),
      Field("sectionEnd",
        OptionType(LongType),
        description = None,
        resolve = js => (js.value \ "sectionEnd").asOpt[Long]),
      Field("startInSentence",
        LongType,
        description = None,
        resolve = js => (js.value \ "startInSentence").as[Long]),
      Field("endInSentence",
        LongType,
        description = None,
        resolve = js => (js.value \ "endInSentence").as[Long]),
      Field("matchedType",
        StringType,
        description = Some("Type of the matched label"),
        resolve = js => (js.value \ "keywordType").as[String])
    )
  )

  val sentenceImp = ObjectType(
    "Sentence",
    fields[Backend, JsValue](
      Field("section",
        StringType,
        description = Some("Section of the publication (either title or abstract)"),
        resolve = js => (js.value \ "section").as[String]),
      Field("matches",
        ListType(matchImp),
        description = Some("List of matches"),
        resolve = js => (js.value \ "matches").as[Seq[JsValue]])
    )
  )

  val publicationImp = ObjectType(
    "Publication",
    fields[Backend, JsValue](
      Field("pmid",
            StringType,
            description = None,
            resolve = js => (js.value \ "pmid").as[String]),
      Field("pmcid",
        OptionType(StringType),
        description = None,
        resolve = js => (js.value \ "pmcid").asOpt[String]),
      Field("publicationDate",
        OptionType(StringType),
        description = Some("Publication Date"),
        resolve = js => (js.value \ "date").asOpt[String]),
      Field("sentences",
        OptionType(ListType(sentenceImp)),
        description = Some("Unique counts per matched keyword"),
        resolve = js => (js.value \ "sentences").asOpt[Seq[JsValue]])
    )
  )
}
