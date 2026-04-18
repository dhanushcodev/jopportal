package com.eazybytes.jobportal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactDto(
        @NotBlank(message = "Email should not be empty")
        @Email
        String email,

        @NotBlank(message = "Message should not be empty")
        @Size(min=5,max=500,message = "Not a valid message")
        String message,

        @NotBlank(message = "Name should not be empty")
        @Size(min=5,max=30,message = "Not a valid name")
        String name,

        @NotBlank(message = "Subject should not be empty")
        @Size(min=5,max=130,message = "Not a valid subject")
        String subject,

        @NotBlank(message = "UserType should not be empty")
        @Pattern(regexp = "Job Seeker|Employeer|Other",message = "Invalid userType")
        String userType) {
}
