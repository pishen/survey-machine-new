package text

import main.Main.logger
import scalax.io.Resource
import java.io.FileWriter
import scala.util.Random

object Tester {
  case class Res(survey: Paper, coEval: Eval, rwrEval: Eval)

  def test(args: Array[String]) = {

    def degreeFilter(survey: Paper) = {
      val base = survey.outgoingPapers
      //degree lower bound
      val check1 = base.forall(p => (p.outgoingPapers ++ p.incomingPapers).size >= 5)
      //avg degree
      val check2 = base.map(p => (p.outgoingPapers ++ p.incomingPapers).size).sum / base.size.toDouble >= 20
      check1 && check2
    }

    val surveys = Paper.allPapers
      .filter(p => {
        //val conf = p.dblpKey.split("-")(1)
        p.year >= 2007 &&
          p.outgoingPapers.size >= 20
        degreeFilter(p)
      }).toSeq

    val ress = surveys.flatMap(survey => {
      logger.info("test survey " + survey.dblpKey)
      val base = survey.outgoingPapers
      val baseSeq = base.toSeq
      val ansSize = (base.size * 0.5).toInt
      
      (1 to 10).flatMap(i => {
        val answers = Random.shuffle(baseSeq).take(ansSize).toSet
        val queries = base.diff(answers)

        val coRanks = Ranker.cocitation(survey, queries, 50)
        val rwrRanks = Ranker.rwr(survey, queries, args(0).toInt, args(1).toDouble, args(2).toDouble, 50)
        Some(Res(survey, Eval.eval(coRanks, answers), Eval.eval(rwrRanks, answers)))
        
        /*val possibleSet = queries.flatMap(_.incomingPapers.filter(_ != survey).flatMap(_.outgoingPapers))
        if (answers.forall(a => possibleSet.contains(a))) {
          val coRanks = Ranker.cocitation(survey, queries, 50)
          val rwrRanks = Ranker.rwr(survey, queries, args(0).toInt, args(1).toDouble, args(2).toDouble, 50)
          Some(Res(survey, Eval.eval(coRanks, answers), Eval.eval(rwrRanks, answers)))
        } else {
          None
        }*/
      })
    })
    val ressSize = ress.size.toDouble

    logger.info("ress-size: " + ress.size)
    logger.info("coMAP: " + (ress.map(_.coEval.ap).sum / ressSize))
    logger.info("coMeanF1: " + (ress.map(_.coEval.f1).sum / ressSize))
    logger.info("coMeanP: " + (ress.map(_.coEval.precision).sum / ressSize))
    logger.info("coMeanR: " + (ress.map(_.coEval.recall).sum / ressSize))
    logger.info("rwrMAP: " + (ress.map(_.rwrEval.ap).sum / ressSize))
    logger.info("rwrMeanF1: " + (ress.map(_.rwrEval.f1).sum / ressSize))
    logger.info("rwrMeanP: " + (ress.map(_.rwrEval.precision).sum / ressSize))
    logger.info("rwrMeanR: " + (ress.map(_.rwrEval.recall).sum / ressSize))
  }
}