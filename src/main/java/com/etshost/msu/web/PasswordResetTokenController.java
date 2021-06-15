package com.etshost.msu.web;

import com.etshost.msu.entity.PasswordResetToken;
import com.etshost.msu.entity.User;
import com.etshost.msu.service.PasswordResetTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the {@link com.etshost.msu.entity.PasswordResetToken} class.
 */
@RequestMapping("/password-reset")
@RestController
public class PasswordResetTokenController {

    @Autowired
    PasswordResetTokenService prtService;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get() {
        ModelAndView mav = new ModelAndView("passwordresettokens/resetPassword");
        // must match the jsp page name which is being requested.
        mav.addObject("greeting", "Here I am");
        return mav;
    }

    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public ModelAndView changePassword(@PathVariable("token") String token) {
        String result = prtService.validatePasswordResetToken(token);
        if (result != null) {
            return new ModelAndView("passwordresettokens/tokenError");
        } else {
            ModelAndView model = new ModelAndView("passwordresettokens/changePassword");
            model.addObject("token", token);
            return model;
        }
    }


    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView post(@RequestParam("email") String userEmail, HttpServletRequest request) {
        User user = null;
        try {
            user = User.findUsersByEmailEquals(userEmail).getSingleResult();
        } catch (Exception e) {
            this.logger.info(e.getMessage());
        }
        String path = request.getRequestURL().toString();
        ModelAndView mav = null;
        if (user == null) {
            mav = new ModelAndView("passwordresettokens/resetPassword");
            mav.addObject("errorMsg", "No user with email: " + userEmail);
        } else {
            PasswordResetToken token = user.createPasswordResetToken();
            prtService.sendTokenEmail(path, token);
            mav = new ModelAndView("passwordresettokens/confirm");
        }
        return mav;
    }


    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public ModelAndView changePassword(@RequestParam("password") String password,
                                       @RequestParam("new_password_two") String password2,
                                       @RequestParam("token") String token) {

        String result = prtService.validatePasswordResetToken(token);
        ModelAndView mav = null;
        if (result != null) {
            mav = new ModelAndView("passwordresettokens/changePassword");
            mav.addObject("token_error", result);
        } else {

            Optional<User> user = prtService.getUserByPasswordResetToken(token);
            if (user.isPresent()) {
                List<String> errors = user.get().changePassword(password);
                if (!errors.isEmpty()) {
                    mav = new ModelAndView("passwordresettokens/changePassword");
                    mav.addObject("password", errors);
                    mav.addObject("token", token);
                } else {
                    mav = new ModelAndView("passwordresettokens/passwordResetSucceess");
                    //user.get().merge();
                    prtService.expireToken(token);
                }

            } else {
                mav = new ModelAndView("passwordresettokens/changePassword");
                mav.addObject("token_error", "invalid");

            }
        }
        return mav;
    }
}
