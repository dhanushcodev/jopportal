package com.eazybytes.jobportal.utility;

import com.eazybytes.jobportal.entity.JobPortalUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ApplicationUtility {

    public static String getLoggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            return "SYSTEM";
        }
        Object principal = authentication.getPrincipal();
        String username = null;
        if(principal instanceof JobPortalUser){ //during login operation principal was set to  JopPortalUser, check -
            //-customAuthenticationProvider function
            username = ((JobPortalUser) principal).getEmail();
        }else { // during jwt validation the principal is set a username, check - jwtTokenFilterValidation function
            username = principal.toString();
        }
        return username;
    }

}
