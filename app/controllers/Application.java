package controllers;

import java.util.List;

import models.Entry;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.createForm;
import views.html.index;

public class Application extends Controller {
  
  public static Result index() {
      List<Entry> entries = Entry.find.all();
      return ok(index.render(entries));
  }
  
  public static Result create() {
      Form<Entry> entryForm = form(Entry.class);
      return ok(
          createForm.render(entryForm)
      );
  }
  
  public static Result save() {
      Form<Entry> entryForm = form(Entry.class).bindFromRequest();
      if(entryForm.hasErrors()) {
          return badRequest(createForm.render(entryForm));
      }
      entryForm.get().save();
      flash("success", "Entry " + entryForm.get().id + " has been created");
      return index();
  }
}