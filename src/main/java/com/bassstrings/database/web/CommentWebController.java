package com.bassstrings.database.web;

import com.bassstrings.database.domain.ReviewComment;
import com.bassstrings.database.domain.StringReview;
import com.bassstrings.database.security.CurrentUserSupport;
import com.bassstrings.database.service.CommentService;
import com.bassstrings.database.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Handles comment creation/removal from the review detail page. */
@Controller
public class CommentWebController {

    private final CommentService commentService;
    private final ReviewService reviewService;

    public CommentWebController(CommentService commentService, ReviewService reviewService) {
        this.commentService = commentService;
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews/{reviewId}/comments")
    public String create(@PathVariable String reviewId, @RequestParam String body,
                          RedirectAttributes redirectAttributes) {
        StringReview review = reviewService.get(reviewId);
        ReviewComment comment = new ReviewComment();
        comment.setBody(body);
        commentService.create(reviewId, comment, CurrentUserSupport.currentUser());
        redirectAttributes.addFlashAttribute("message", "Comment posted.");
        return "redirect:/strings/" + review.getBassStringId();
    }

    @PostMapping("/comments/{commentId}/delete")
    public String delete(@PathVariable String commentId,
                          @RequestParam String bassStringId,
                          RedirectAttributes redirectAttributes) {
        commentService.delete(commentId, CurrentUserSupport.currentUser());
        redirectAttributes.addFlashAttribute("message", "Comment removed.");
        return "redirect:/strings/" + bassStringId;
    }
}
