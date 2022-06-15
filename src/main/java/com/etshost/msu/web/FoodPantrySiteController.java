package com.etshost.msu.web;

import java.io.InputStreamReader;
import java.util.List;

import com.etshost.msu.entity.FoodPantrySite;
import com.opencsv.bean.CsvToBeanBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
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
		List<FoodPantrySite> results = FoodPantrySite.findFoodPantrySiteEntries(start, length, orderField, orderDir);
        
		return FoodPantrySite.toJsonArray(results);
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
            sites = new CsvToBeanBuilder(isr)
                .withType(FoodPantrySite.class).build().parse();
			FoodPantrySite.replaceFoodPantrySiteEntries(sites);
        } catch (Exception e) {
            //TODO: handle exception
            logger.error(e.toString());
        } finally {
			ModelAndView mav = new ModelAndView("foodpantrysites/list");
			mav.addObject("foodpantrysites", FoodPantrySite.findAllFoodPantrySites());
			return mav;
		}
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
