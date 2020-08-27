package services.mongodb

import javax.inject.{Inject, Singleton}
import services.{DI, DatasetService, SelectionService}
import salat.dao.{ModelCompanion, SalatDAO}
import com.mongodb.casbah.Imports._
import play.api.Play._

import scala.Some
import com.mongodb.casbah.commons.MongoDBObject
import play.api.Logger
import models.{Dataset, Selected, UUID}
import org.bson.types.ObjectId
import services.mongodb.MongoContext.context

/**
 * Created by lmarini on 4/24/14.
 */
@Singleton
class MongoDBSelectionService @Inject() (datasets: DatasetService)  extends SelectionService {

  def add(dataset: UUID, user: String) {
    Logger.debug("Selecting dataset " + dataset)
    val query = MongoDBObject("user" -> user)
    val update = $addToSet("datasets" -> dataset.stringify)
    val updated = SelectedDAO.dao.collection.findAndModify(
      query = query,
      update = update,
      upsert = true,
      fields = null,
      sort = null,
      remove = false,
      returnNew = true
    )
    Logger.debug("Selected updated " +  updated)
  }

  def remove(dataset: UUID, user: String) {
    Logger.debug("Removing dataset " + dataset + " from user selection " + user)
    val query = MongoDBObject("user" -> user)
    val update = $pull("datasets" -> dataset.stringify)
    SelectedDAO.dao.collection.update(query, update, upsert=true)
  }

  def get(user: String): List[Dataset] = {
    SelectedDAO.findOne(MongoDBObject("user"->user)) match {
      case Some(selected) => datasets.get(selected.datasets.map(UUID(_))).found
      case None => List.empty
    }
  }

  def deleteAll(user: String) = {}

  def downloadAll(user: String) = {}
}

object SelectedDAO extends ModelCompanion[Selected, ObjectId] {
  val COLLECTION = "selected"
  val mongos: MongoStartup = DI.injector.getInstance(classOf[MongoStartup])
  val dao = new SalatDAO[Selected, ObjectId](collection = mongos.collection(COLLECTION)) {}
}
