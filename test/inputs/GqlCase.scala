package inputs

import models.entities.AggregationFilter
import org.scalacheck.Gen

sealed trait GqlCase[T] extends GqlItTestInputs {
  val file: String
  val inputGenerator: Gen[T]

  def generateVariables(inputs: T): String
}

case class Disease(file: String) extends GqlCase[String] {
  val inputGenerator = diseaseGenerator

  def generateVariables(disease: String): String =
    s"""
      "variables": {
      "efoId": "$disease",
      "size": 10,
      "index": 0
    }
    """
}

case class DiseaseAggregationfilter(file: String) extends GqlCase[(String, AggregationFilter)] {
  val inputGenerator = for {
    disease <- diseaseGenerator
    agg <- aggregationfilterGenerator
  } yield (disease, agg)

  def generateVariables(inputs: (String, AggregationFilter)): String =
    s"""
      "variables": {
      "efoId": "${inputs._1}",
      "aggregationFilters": [
        {
        "name": "${inputs._2.name}",
        "path": ${inputs._2.path.mkString("[\"", "\", \"", "\"]")}
        }]
    }
    """
}

case class Drug(file: String) extends GqlCase[String] {
  override val inputGenerator = drugGenerator

  override def generateVariables(drugId: String): String = {
    s"""
      "variables": {
      "chemblId": "$drugId"
    }
    """
  }
}

case class Search(file: String) extends GqlCase[String] {
  override val inputGenerator = searchGenerator

  def generateVariables(searchTerm: String): String = {
    s"""
      "variables": {
      "queryString": "$searchTerm"
    }
    """
  }
}

case class SearchPage(file: String) extends GqlCase[(String, String, Int)] {
  val inputGenerator = for {
    query <- searchGenerator
    entities <- Gen
      .atLeastOne(Seq("target", "disease", "drug"))
      .map(_.mkString("[\"", "\", \"", "\"]"))
    page <- sizeGenerator
  } yield (query, entities, page)

  def generateVariables(inputs: (String, String, Int)): String =
    s"""
      "variables": {
      "queryString": "${inputs._1}",
      "index": ${inputs._3},
      "entityNames": ${inputs._2}
    }
    """
}

case class Target(file: String) extends GqlCase[String] {

  val inputGenerator = geneGenerator

  def generateVariables(target: String): String = {
    s"""
      "variables": {
      "ensgId": "$target",
      "size": 10,
      "index": 0
    }
    """
  }
}

case class TargetDisease(file: String) extends GqlCase[(String, String)] {
  val inputGenerator = for {
    gene <- geneGenerator
    disease <- diseaseGenerator
  } yield (gene, disease)

  def generateVariables(inputs: (String, String)): String = {
    val (target, disease) = inputs
    s"""
      "variables": {
      "efoId": "$target",
      "ensemblId": "$disease"
    }
    """
  }
}

case class TargetDiseaseSize(file: String) extends GqlCase[(String, String, Int)] {
  val inputGenerator = targetDiseaseSizeGenerator

  def generateVariables(input: (String, String, Int)): String =
    generateVariables(input._1, input._2, input._3)

  def generateVariables(target: String, disease: String, size: Int): String = {
    s"""
      "variables": {
      "efoId": "$target",
      "ensemblId": "$disease",
      "size": $size
    }
    """
  }
}

case class TargetDiseaseSizeCursor(file: String) extends GqlCase[(String, String, Int)] {
  val inputGenerator = targetDiseaseSizeGenerator

  def generateVariables(input: (String, String, Int)): String =
    generateVariables(input._1, input._2, input._3)

  def generateVariables(target: String, disease: String, size: Int): String = {
    s"""
    "variables": {
      "efoId": "$target",
      "ensemblId": "$disease",
      "size": $size,
      "cursor": null
    }
  """
  }
}
