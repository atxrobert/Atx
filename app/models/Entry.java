package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity 
public class Entry extends Model {
    
  @Id
  public Long id;

  @Constraints.Required
  public EntryType entryType;
  
  @Constraints.Required
  public String text;
  
  @Constraints.Required
  public String settings;
  
  public static Finder<Long,Entry> find = new Finder(Long.class, Entry.class);

}