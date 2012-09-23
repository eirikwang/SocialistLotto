package org.clarity.demo.cqrs.events

import com.hazelcast.core.{EntryEvent, EntryListener, ItemListener}
import org.clarity.demo.cqrs.domain.Project

class ProjectListener extends EntryListener[Long, Project]{


/*public static void main(String[] args) {
      Sample sample = new Sample();
      Queue queue = Hazelcast.getQueue ("default");
      Map   map   = Hazelcast.getMap   ("default");
      Set   set   = Hazelcast.getSet   ("default");
      //listen for all added/updated/removed entries
      queue.addItemListener(sample, true);
      set.addItemListener  (sample, true);
      map.addEntryListener (sample, true);
      //listen for an entry with specific key
      map.addEntryListener (sample, "keyobj");
    }
*/
  def entryAdded(event: EntryEvent[Long, Project]) {}

  def entryRemoved(event: EntryEvent[Long, Project]) {}

  def entryUpdated(event: EntryEvent[Long, Project]) {}

  def entryEvicted(event: EntryEvent[Long, Project]) {}
}
