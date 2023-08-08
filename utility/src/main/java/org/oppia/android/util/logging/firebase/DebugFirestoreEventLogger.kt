package org.oppia.android.util.logging.firebase

import org.oppia.android.app.model.EventLog
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A debug implementation of [FirestoreEventLogger] used in developer-only builds of the event.
 *
 * It forwards events to a production [FirestoreEventLogger] for real logging, but it also records logged
 * events for later retrieval (e.g. via [getEventList]).
 */
@Singleton
class DebugFirestoreEventLogger @Inject constructor(
  factory: FirestoreEventLoggerProdImpl.Factory
) : FirestoreEventLogger {
  private val realEventLogger by lazy { factory.createFirestoreEventLogger() }
  private val eventList = CopyOnWriteArrayList<EventLog>()

  override fun uploadEvent(eventLog: EventLog) {
    eventList.add(eventLog)
    realEventLogger.uploadEvent(eventLog)
  }

  /** Returns the list of all [EventLog]s logged since the app opened. */
  fun getEventList(): List<EventLog> = eventList

  /** Returns the most recently logged event. */
  fun getMostRecentEvent(): EventLog = getEventList().last()
}