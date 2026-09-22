package com.bassstrings.database.web;

import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.api.model.StringConstruction;
import com.bassstrings.database.api.model.StringMaterial;
import com.bassstrings.database.domain.BassGuitarModel;
import com.bassstrings.database.domain.BassStringProduct;
import com.bassstrings.database.domain.ReviewComment;
import com.bassstrings.database.domain.StringReview;
import com.bassstrings.database.security.CurrentUserSupport;
import com.bassstrings.database.service.BassModelService;
import com.bassstrings.database.service.BassStringSearchCriteria;
import com.bassstrings.database.service.BassStringService;
import com.bassstrings.database.service.CommentService;
import com.bassstrings.database.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;

/** Public, unauthenticated browsing: the catalog listing, filtering, and detail pages. */
@Controller
public class CatalogWebController {

    private final BassStringService bassStringService;
    private final BassModelService bassModelService;
    private final ReviewService reviewService;
    private final CommentService commentService;

    public CatalogWebController(BassStringService bassStringService, BassModelService bassModelService,
                                 ReviewService reviewService, CommentService commentService) {
        this.bassStringService = bassStringService;
        this.bassModelService = bassModelService;
        this.reviewService = reviewService;
        this.commentService = commentService;
    }

    @GetMapping({"/", "/strings"})
    public String catalog(@RequestParam(required = false) String brand,
                           @RequestParam(required = false) StringMaterial material,
                           @RequestParam(required = false) StringConstruction construction,
                           @RequestParam(required = false) Integer numStrings,
                           @RequestParam(required = false) PlayingStyle playingStyle,
                           @RequestParam(required = false) MusicGenre genre,
                           @RequestParam(required = false) String q,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        BassStringSearchCriteria criteria = new BassStringSearchCriteria(
                brand, material, construction, numStrings, playingStyle, genre, q);
        Pageable pageable = PageRequest.of(Math.max(page, 0), 12, Sort.by(Sort.Direction.DESC, "averageOverallRating"));

        model.addAttribute("results", bassStringService.search(criteria, pageable));
        model.addAttribute("criteria", criteria);
        model.addAttribute("materials", StringMaterial.values());
        model.addAttribute("constructions", StringConstruction.values());
        model.addAttribute("playingStyles", PlayingStyle.values());
        model.addAttribute("genres", MusicGenre.values());
        model.addAttribute("currentUser", CurrentUserSupport.currentUserOrNull());
        return "catalog";
    }

    @GetMapping("/strings/{id}")
    public String detail(@PathVariable String id,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        BassStringProduct product = bassStringService.get(id);
        Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StringReview> reviews = reviewService.listForBassString(id, pageable);

        // One comments query per review on the page - fine at MVP/page-of-10 scale; a
        // production version would batch this with an IN query keyed by review id.
        Map<String, java.util.List<ReviewComment>> commentsByReview = new LinkedHashMap<>();
        for (StringReview review : reviews.getContent()) {
            commentsByReview.put(review.getId(),
                    commentService.listForReview(review.getId(), Pageable.unpaged()).getContent());
        }

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("commentsByReview", commentsByReview);
        model.addAttribute("summary", reviewService.computeRatingSummary(id));
        model.addAttribute("reviewForm", new ReviewFormData());
        model.addAttribute("genres", MusicGenre.values());
        model.addAttribute("currentUser", CurrentUserSupport.currentUserOrNull());
        return "string-detail";
    }

    @GetMapping("/models")
    public String models(@RequestParam(required = false) String brand,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), 12, Sort.by(Sort.Direction.ASC, "brand"));
        model.addAttribute("results", bassModelService.search(brand, null, pageable));
        model.addAttribute("brand", brand);
        model.addAttribute("currentUser", CurrentUserSupport.currentUserOrNull());
        return "models";
    }

    @GetMapping("/models/{id}")
    public String modelDetail(@PathVariable String id, Model model) {
        BassGuitarModel bassModel = bassModelService.get(id);
        model.addAttribute("bassModel", bassModel);
        model.addAttribute("relatedReviews", reviewService.listForBassModel(id));
        model.addAttribute("currentUser", CurrentUserSupport.currentUserOrNull());
        return "model-detail";
    }
}
