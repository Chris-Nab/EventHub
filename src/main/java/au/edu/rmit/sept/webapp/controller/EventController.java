package au.edu.rmit.sept.webapp.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;
import au.edu.rmit.sept.webapp.service.EventService;

@Controller
public class EventController {
    private final EventService eventService;
    private final CategoryRepository categoryRepository;

    public EventController(EventService Service, CategoryRepository categoryRepository)
    {
      this.eventService = Service;
      this.categoryRepository = categoryRepository;
    }
  
  //Create Event
  @GetMapping("/eventPage")
    public String eventPage(Model model) {
      List<EventCategory> categories = categoryRepository.findAll();
      model.addAttribute("categories", categories);
      model.addAttribute("event", new Event());
      model.addAttribute("isEdit", false);
      return "eventPage";
    }

  @PostMapping("/eventForm")
    public String submitEvent(@ModelAttribute("event") Event event, @RequestParam(name = "categoryIds", required = false) List<Long> categoryIds, Model model, RedirectAttributes redirectAttributes) {
    event.setCreatedByUserId(5L);
      if (categoryIds == null) categoryIds = java.util.List.of();
      // server-side cap (mirrors the JS)
    if (categoryIds.size() > 3) {
      model.addAttribute("confirmation", "You can select up to 3 categories only.");
  } else if (!eventService.isValidDateTime(event)) {
      model.addAttribute("confirmation", "Enter a valid date!");
  } else {
      // fetch names for duplicate check
      List<String> categoryNames = categoryRepository.findNamesByIds(categoryIds);

      boolean exists = eventService.eventExist(
          event.getCreatedByUserId(),
          event.getName(),
          categoryNames,             
          event.getLocation()
      );

        if (!exists) {
            eventService.saveEventWithCategories(event, categoryIds); 
            model.addAttribute("confirmation", "Event created successfully!");
        } else {
          model.addAttribute("confirmation", "Event already exists!");
        }

          // re-populate form
          model.addAttribute("event", new Event());
          model.addAttribute("categories", categoryRepository.findAll());
          model.addAttribute("isEdit", false);
          return "eventPage";
        }
      model.addAttribute("event", event);
      model.addAttribute("isEdit", false);
      return "eventPage";
    }

    // Edit form
    @GetMapping("/event/edit/{id}")
    public String editEvent(@PathVariable("id") Long eventId, Model model)
    {
      Event event = eventService.findById(eventId);
      model.addAttribute("event", event);
      model.addAttribute("isEdit", true);

      //Format date in order to render it to the event form
      if (event.getDateTime() != null) {
        String formattedDateTime = event.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        model.addAttribute("formattedDateTime", formattedDateTime);
      } else {
          model.addAttribute("formattedDateTime", "");
      }
 
      return "eventPage";
    }

    @PostMapping("/event/edit/{id}")
    public String updateEvent(@PathVariable("id") Long eventId, Event event, RedirectAttributes redirectAttributes, Model model)
    {

      if(eventService.isValidDateTime(event))
      {
        event.setEventId(eventId);
        event.setCreatedByUserId(5L);
        event.setDateTime(event.getDateTime());
        eventService.updateEvent(event);
        model.addAttribute("isEdit", true);
        redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
        return "redirect:/";
      }
      else {
        model.addAttribute("confirmation", "Enter a valid date!");
        model.addAttribute("event", event);
        model.addAttribute("isEdit", true);
        return "eventPage";
      }
    }

    @PostMapping("/event/delete/{id}")
    public String deleteEvent(@PathVariable("id") long eventId, RedirectAttributes redirectAttributes)
    {
      eventService.deleteEventbyId(eventId);
      redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully!");
      return "redirect:/";
    }


}
