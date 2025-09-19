package com.example.financial_management.model.auth;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "Authentication response")
public class AuthResponse {

    @SerializedName("status")
    @Schema(description = "Response status", example = "success")
    private String status;

    @SerializedName("email")
    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @SerializedName("token")
    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @SerializedName("error")
    @Schema(description = "Error message if any", example = "null")
    private String error;

    @SerializedName("error_id")
    @Schema(description = "Error identifier", example = "null")
    private String errorId;
}
