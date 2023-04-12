package com.etshost.msu.web;

import java.io.InputStreamReader;
import java.util.List;

import com.etshost.msu.entity.FoodPantrySite;
import com.etshost.msu.entity.User;
import com.etshost.msu.entity.Viewing;
import com.opencsv.bean.CsvToBeanBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the {@link com.etshost.msu.entity.FoodPantrySite} class.
 */
@RequestMapping("/foodpantrysites")
@RestController
public class FoodPantrySiteController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FoodPantrySite repository;
	
	/**
	 * Returns JSON list of FoodPantrySites
	 * @param start			index of first item
	 * @param length		number of items to return
	 * @param orderField	field to order results by
	 * @param orderDir		order direction (ASC or DESC)
	 * @return				JSON array of results
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
	public String list(
			@RequestParam(name = "start", defaultValue = "0") int start,
			@RequestParam(name = "length", defaultValue = "-1") int length,
			@RequestParam(name = "orderField", required = false) String orderField,
			@RequestParam(name = "orderDir", defaultValue = "ASC") String orderDir) {
		if(length < 0) {
			return repository.findAllFoodPantrySitesJson();
		} else {
			List<FoodPantrySite> results = repository.findFoodPantrySiteEntries(start, length, orderField, orderDir);
        	return FoodPantrySite.toJsonArray(results);
		}
	}

	@RequestMapping(
		value = { "/list"}, 
		method = RequestMethod.GET, produces = "text/html")
	public ModelAndView listPage(ModelMap model) {
		ModelAndView mav = new ModelAndView("foodpantrysites/list");
		mav.addObject("foodpantrysites", FoodPantrySite.findAllFoodPantrySites());
		return mav;
	}
  
	/**
	 * Returns JSON representation of FoodPantrySite with the given ID
	 * @param id	ID of FoodPantrySite to view 
	 * @return		JSON of FoodPantrySite
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("id") long id) {
		FoodPantrySite fps = FoodPantrySite.findFoodPantrySite(id);
		if (fps == null) {
			return "0";
		}

		new Viewing(User.getLoggedInUser(), fps.getId()).persist();
		return fps.toJson();
	}
		
	/**
	 * Upload a CSV list of FoodPantrySites
     * Replaces all pantries with those in the list
	 * @param start			index of first item
	 * @param length		number of items to return
	 * @param orderField	field to order results by
	 * @param orderDir		order direction (ASC or DESC)
	 * @return				JSON array of results
	 */
	@Transactional
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json")
    public ModelAndView upload(@RequestParam("file") MultipartFile file) {
        List<FoodPantrySite> sites;
        try(InputStreamReader isr = new InputStreamReader(file.getInputStream())) {
            sites = new CsvToBeanBuilder<FoodPantrySite>(isr)
				.withType(FoodPantrySite.class)
                .build().parse();
			repository.replaceFoodPantrySiteEntries(sites);
        } catch (Exception e) {
            //TODO: handle exception
            logger.error(e.toString());
		}
		ModelAndView mav = new ModelAndView("foodpantrysites/list");
		mav.addObject("foodpantrysites", FoodPantrySite.findAllFoodPantrySites());
		return mav;
	}


	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(
		value = { "/upload"}, 
		method = RequestMethod.GET, produces = "text/html")
	public ModelAndView uploadPage(ModelMap model) {
		ModelAndView mav = new ModelAndView("foodpantrysites/upload");
		return mav;
	}

}
