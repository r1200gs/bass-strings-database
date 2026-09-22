package com.bassstrings.database.web;

import com.bassstrings.database.domain.StringReview;
import com.bassstrings.database.security.CurrentUserSupport;
import com.bassstrings.database.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Handles the "write a review" form and author/monitor/admin review removal from the web UI. */
@Controller
public class ReviewWebController {

    private final ReviewService reviewService;

    public ReviewWebController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/strings/{bassStringId}/reviews")
    public String create(@PathVariable String bassStringId,
                          @ModelAttribute ReviewFormData form,
                          RedirectAttributes redirectAttributes) {
        reviewService.create(bassStringId, form.toEntity(), CurrentUserSupport.currentUser());
        redirectAttributes.addFlashAttribute("message", "Thanks - your review is live.");
        return "redirect:/strings/" + bassStringId;
    }

    @PostMapping("/reviews/{reviewId}/delete")
    public String delete(@PathVariable String reviewId, RedirectAttributes redirectAttributes) {
        StringReview review = reviewService.get(reviewId);
        String bassStringId = review.getBassStringId();
        reviewService.delete(reviewId, CurrentUserSupport.currentUser());
        redirectAttributes.addFlashAttribute("message", "Review removed.");
        return "redirect:/strings/" + bassStringId;
    }
}
