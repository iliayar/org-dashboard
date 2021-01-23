package org.dashboard.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.dashboard.*

object Users : Table() {
  val id = integer("id").autoIncrement()
  val name = varchar("name", length = 50)
  val password = varchar("password", length = 256)

  override val primaryKey = PrimaryKey(id)
}

object Documents : Table() {
  val id = integer("id").autoIncrement()
  val name = varchar("name", length=100)
  val content = text("content")
  val userId = (integer("user_id") references Users.id)

  override val primaryKey = PrimaryKey(id)
}

object Sessions : Table() {
  val token = varchar("token", length=256)
  val userId = (integer("user_id") references Users.id).nullable()

  override val primaryKey = PrimaryKey(token)
}

class DashboardDB {
  init {
      val SQL_HOST = System.getenv("SQL_HOST") ?: "db"
      val SQL_PORT = System.getenv("SQL_PORT") ?: "5431"
      val SQL_USER = System.getenv("SQL_USER") ?: "postgres"
      val SQL_PASSWORD = System.getenv("SQL_PASSWORD") ?: ""
      val SQL_DB = System.getenv("SQL_DB") ?: "dashboard"

      Database.connect("jdbc:postgresql://$SQL_HOST:$SQL_PORT/$SQL_DB", driver = "org.postgresql.Driver", user = SQL_USER, password = SQL_PASSWORD)
      transaction {
        SchemaUtils.create(Users, Documents, Sessions)
      }
  }

  fun checkUser(name: String, password: String): Int? {
    var res: Int? = null
    transaction {
        val user = Users.select {
          (Users.name eq name) and (Users.password eq password)
        }.limit(1)
        if(user.count() > 0) {
          res = user.first().get(Users.id)
        }
    }
    return res
  }

  fun addUser(name: String, password: String): Int? {
    var res: Int? = null
    transaction {
        val cnt = Users.select { Users.name eq name }.count()
      if(cnt == 0.toLong()) {
          res = Users.insert {
            it[Users.name] = name
            it[Users.password] = password
          } get Users.id
      }
    }
    return res
  }

  fun authorize(userId: Int): String {
    var token: String = generateToken()
    transaction {
      Sessions.deleteWhere { Sessions.userId eq userId }
      Sessions.insert {
        it[Sessions.token] = token
        it[Sessions.userId] = userId
      }
    }
    return token
  }

  fun getUser(userId: Int): User {
    return transaction {
      val user = Users.select { Users.id eq userId }.first()
      User(user.get(Users.name))
    }
  }

  fun getDocuments(userId: Int): List<Document> {
    return transaction {
      Documents.select { Documents.userId eq userId }
      .map { doc -> Document(doc.get(Documents.id), doc.get(Documents.name), userId, doc.get(Documents.content))}
    }
  }

  fun getSession(token: String): Int? {
    return transaction {
      Sessions.select { Sessions.token eq token }.first().get(Sessions.userId)
    }
  }

  fun getDocument(docId: Int): Document {
    return transaction {
      val doc = Documents.select { Documents.id eq docId }.first()
      Document(docId, doc.get(Documents.name), doc.get(Documents.userId), doc.get(Documents.content))
    }
  }

  fun saveDocument(doc: Document) {
    transaction {
      Documents.update({ Documents.id eq doc.id!! }) {
        it[Documents.name] = doc.name
        it[Documents.content] = doc.content
      }
    }
  }

  fun deleteDocument(doc: Document) {
    transaction {
      Documents.deleteWhere { Documents.id eq doc.id!! }
    }
  }

  fun createDocument(doc: Document): Int {
    return transaction {
      Documents.insert {
        it[Documents.name] = doc.name
        it[Documents.content] = doc.content
        it[Documents.userId] = doc.user!!
      } get Documents.id
    }
  }
}
