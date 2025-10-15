# Backend API Specification: Project Inquiry Form

## Overview
This document describes the API endpoint for receiving project inquiry form submissions from the EB-5 Guide Android application.

## Endpoint

**POST** `https://news-service.replit.app/api/project-form`

**Status:** ✅ CONNECTED (Production)

## Configuration

Current configuration in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "PROJECT_FORM_ENDPOINT", "\"https://news-service.replit.app/api/project-form\"")
buildConfigField("String", "PROJECT_FORM_API_KEY", "\"\"") // API key not required - public endpoint
```

## Request Headers

```
Content-Type: application/json; charset=UTF-8
```

**Note:** API key authentication is NOT required for this public endpoint. The app will not send `x-api-key` header since `PROJECT_FORM_API_KEY` is empty.

## Request Body (JSON)

```json
{
  "project_id": "project-1760445052726",
  "project_title": "21 Hollywood EDITED",
  "first_name": "John",
  "last_name": "Smith",
  "email": "john.smith@example.com",
  "phone": "+1 (555) 123-4567",
  "country_of_birth": "China",
  "country_of_living": "United States",
  "current_visa_status": "H-1B",
  "accredited_investor": true,
  "timestamp": 1760445052726
}
```

## Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `project_id` | String | Yes | Unique identifier of the EB-5 project |
| `project_title` | String | Yes | Title of the EB-5 project |
| `first_name` | String | Yes | Investor's first name |
| `last_name` | String | Yes | Investor's last name |
| `email` | String | Yes | Investor's email (validated format) |
| `phone` | String | Yes | Investor's phone number (min 7 digits) |
| `country_of_birth` | String | Yes | Investor's country of birth |
| `country_of_living` | String | Yes | Investor's current country of residence |
| `current_visa_status` | String | Yes | Current visa status (see options below) |
| `accredited_investor` | Boolean | Yes | Confirms accredited investor status |
| `timestamp` | Long | Yes | Unix timestamp (milliseconds) when form was submitted |

## Visa Status Options

The `current_visa_status` field will contain one of the following values:

- `H-1B` - Specialty occupation workers
- `F-1` - Student visa
- `B-1/B-2` - Business/Tourist visa
- `O-1` - Individuals with extraordinary ability
- `L-1` - Intracompany transferee
- `J-1` - Exchange visitor
- `E-2` - Treaty investor
- `TN` - NAFTA professional
- `U.S. Green Card` - Permanent resident
- `U.S. Citizen` - U.S. citizenship
- `Other` - Other visa status

## Response Format

### Success Response (201 Created)

```json
{
  "success": true,
  "message": "Form submitted successfully",
  "inquiry_id": "inq_1760445052726_abc123xyz"
}
```

**Status Code:** `201 Created`

### Error Response (4xx/5xx)

```json
{
  "success": false,
  "error": "Invalid email format",
  "field": "email"
}
```

**Status Codes:**
- `400 Bad Request` - Invalid data
- `500 Internal Server Error` - Server error

## Client Behavior

### Validation
The Android app performs the following validations before sending:

- **First Name**: Not blank
- **Last Name**: Not blank
- **Email**: Valid email format (uses Android `Patterns.EMAIL_ADDRESS`)
- **Phone**: Minimum 7 digits
- **Country of Birth**: Not blank
- **Country of Living**: Not blank
- **Visa Status**: Must select from dropdown
- **Accredited Investor**: Must be checked

### Submission Flow

1. User fills out the form
2. User clicks "SUBMIT" button
3. App shows loading indicator (CircularProgressIndicator)
4. App sends POST request with 15s connection timeout, 20s read timeout
5. Based on response:
   - **Success (2xx)**:
     - Form is cleared
     - Toast message: "Submitted successfully"
   - **Error (non-2xx or network error)**:
     - Toast message with error details
     - Form is NOT cleared
     - Focus scrolls to first invalid field

### Network Configuration

```kotlin
connectTimeout = 15000 ms  // 15 seconds
readTimeout = 20000 ms     // 20 seconds
```

## Security Considerations

1. **API Key**: Store securely and rotate regularly
2. **HTTPS Only**: Endpoint must use HTTPS in production
3. **Rate Limiting**: Implement rate limiting per IP/user to prevent abuse
4. **Input Validation**: Always validate on server-side (client validation is not sufficient)
5. **GDPR/Privacy**: Store PII securely and comply with data protection regulations

## Database Schema Suggestion

```sql
CREATE TABLE project_inquiries (
    id VARCHAR(255) PRIMARY KEY,
    project_id VARCHAR(255) NOT NULL,
    project_title VARCHAR(500) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    country_of_birth VARCHAR(255) NOT NULL,
    country_of_living VARCHAR(255) NOT NULL,
    current_visa_status VARCHAR(100) NOT NULL,
    accredited_investor BOOLEAN NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    INDEX idx_project_id (project_id),
    INDEX idx_email (email),
    INDEX idx_submitted_at (submitted_at)
);
```

## Testing

### Example cURL Request

```bash
curl -X POST https://news-service.replit.app/api/project-form \
  -H "Content-Type: application/json" \
  -d '{
    "project_id": "test-project-123",
    "project_title": "Test EB-5 Project",
    "first_name": "John",
    "last_name": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1 555-0123",
    "country_of_birth": "China",
    "country_of_living": "United States",
    "current_visa_status": "H-1B",
    "accredited_investor": true,
    "timestamp": 1760445052726
  }'
```

### Test Cases

1. **Valid Submission**: All required fields with valid data → 201 Created
2. **Missing Field**: Omit required field → 400 Bad Request
3. **Invalid Email**: Invalid email format → 400 Bad Request
4. **Server Error**: Simulate server error → 500 Internal Server Error

## Notification Flow (Recommended)

When a form is submitted successfully:

1. **Save to Database**: Store inquiry in database
2. **Send Notification Email**:
   - To: Project developer/admin
   - Subject: "New EB-5 Inquiry for [Project Title]"
   - Body: Include all form details
3. **Auto-Reply Email**:
   - To: Investor email
   - Subject: "Thank you for your interest in [Project Title]"
   - Body: Confirmation message with next steps
4. **CRM Integration** (Optional): Push lead to CRM system

## Support

For questions about API integration, contact: [your-contact-email]

---

**Document Version:** 1.0
**Last Updated:** 2025-10-15
**Android App Version:** 1.0
