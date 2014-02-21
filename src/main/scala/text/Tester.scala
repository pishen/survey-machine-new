package text

import main.Main.logger
import scalax.io.Resource
import java.io.FileWriter
import scala.util.Random

object Tester {
  def test() = {
    val surveys = Paper.allPapers
      .filter(p => {
        val conf = p.dblpKey.split("-")(1)
        p.year >= 2007 && p.outgoingPapers.size >= 20 && conf == "wsdm"
        /*(conf == "wsdm" || conf == "www" || conf == "sigir" || conf == "cikm" || conf == "kdd")*/
      }).toSeq
    logger.info("wsdm >= 2007 size: " + surveys.size)

    val res = surveys.flatMap(survey => {
      println("testing on survey " + survey.dblpKey)
      val citedBySurvey = survey.outgoingPapers
      val ansSize = (citedBySurvey.size * 0.1).toInt
      (1 to 10).map(i => {
        val answers = Random.shuffle(citedBySurvey).take(ansSize)
        val queries = citedBySurvey.diff(answers)
        val ranklist = Ranker.cocitation(survey, queries, 50)
        val ap = Eval.computeAP(ranklist, answers)
        val f1 = Eval.computeF1(ranklist, answers)
        val rr = Eval.computeRR(ranklist, answers)
        (ap, f1, rr)
      })
    })
    val map = res.map(_._1).sum / res.size.toDouble
    val meanF1 = res.map(_._2).sum / res.size.toDouble
    val mrr = res.map(_._3).sum / res.size.toDouble
    logger.info("MAP: " + map)
    logger.info("meanF1: " + meanF1)
    logger.info("MRR: " + mrr)
  }
}