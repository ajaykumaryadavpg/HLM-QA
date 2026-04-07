"""
generate_swagger.py
Generates HLM-API-Swagger.yaml — OpenAPI 3.0 spec for all HLM Platform APIs.
Importable into Swagger UI, Redoc, Insomnia, Postman, or any OpenAPI-compatible tool.

Run: python3 generate_swagger.py
"""

import yaml, json, os

GRAPHQL_URL = "https://rw3sl7mwgzcvbg7zuyqj7ppjv4.appsync-api.us-east-2.amazonaws.com/graphql"
MAP_BASE    = "https://maps.geo.us-east-2.amazonaws.com"
MAP_NAME    = "HlmMapOpenData-a3b4d250-131a-11f1-90d8-0aa113e32f65"
CLIENT_ID   = "3aseu1rf3q3tae7u4dgplllii8"
POOL_ID     = "us-east-2_Q36YWNsEC"
COGNITO_URL = "https://cognito-idp.us-east-2.amazonaws.com/"
OUTPUT      = "/Users/ajaykumar.yadav/HLM-QA/HLM-API-Swagger.yaml"


# ── helpers ───────────────────────────────────────────────────────────────────
def gql_body(operation_name, query, variables):
    return {"operationName": operation_name,
            "query":         query.strip(),
            "variables":     variables}


def graphql_path(operation_name, http_method, summary, description,
                 tags, query_str, variables, responses_extra=None,
                 deprecated=False, warning=None):
    """Return an OpenAPI path item dict for one GraphQL operation."""
    full_desc = description
    if warning:
        full_desc = f"⚠️ **{warning}**\n\n{description}"

    body = gql_body(operation_name, query_str, variables)
    body_json = json.dumps(body, indent=2)

    path_item = {
        "post": {
            "operationId": operation_name,
            "summary":     summary,
            "description": full_desc,
            "tags":        tags,
            "deprecated":  deprecated,
            "security":    [{"CognitoIdToken": []}],
            "requestBody": {
                "required": True,
                "content": {
                    "application/json": {
                        "schema": {"$ref": "#/components/schemas/GraphQLRequest"},
                        "example": body
                    }
                }
            },
            "responses": {
                "200": {
                    "description": "Successful GraphQL response",
                    "content": {
                        "application/json": {
                            "schema": {"$ref": "#/components/schemas/GraphQLResponse"},
                            "example": {"data": {operation_name: {"items": "[]", "totalCount": 0, "nextToken": None}}}
                        }
                    }
                },
                "400": {"$ref": "#/components/responses/BadRequest"},
                "401": {"$ref": "#/components/responses/Unauthorized"},
            }
        }
    }
    if responses_extra:
        path_item["post"]["responses"].update(responses_extra)
    return path_item


# ═════════════════════════════════════════════════════════════════════════════
# Query strings (single-line for compact YAML)
# ═════════════════════════════════════════════════════════════════════════════
Q = {
"listDevices": """query listDevices($status: String, $limit: Int, $nextToken: String) {
  listDevices(status: $status, limit: $limit, nextToken: $nextToken) {
    items
    nextToken
    totalCount
  }
}""",

"getDevice": """query getDevice($id: String!) {
  getDevice(id: $id) {
    id
    deviceName
    serialNumber
    model
    status
    location
    firmwareVersion
  }
}""",

"getDevicesByCustomer": """query getDevicesByCustomer($customerId: String!) {
  getDevicesByCustomer(customerId: $customerId) {
    items
    totalCount
  }
}""",

"getDevicesByLocation": """query getDevicesByLocation($location: String!) {
  getDevicesByLocation(location: $location) {
    items
    totalCount
  }
}""",

"listFirmware": """query listFirmware($status: String, $limit: Int, $nextToken: String) {
  listFirmware(status: $status, limit: $limit, nextToken: $nextToken) {
    items
    nextToken
    totalCount
  }
}""",

"getFirmware": """query getFirmware($id: String!) {
  getFirmware(id: $id) {
    id
    name
    version
    deviceModel
    status
    releaseDate
    fileSize
    checksum
  }
}""",

"getFirmwareByModel": """query getFirmwareByModel($deviceModel: String!) {
  getFirmwareByModel(deviceModel: $deviceModel) {
    items
    totalCount
  }
}""",

"getFirmwareWithRelations": """query getFirmwareWithRelations($id: String!) {
  getFirmwareWithRelations(id: $id) {
    items
    totalCount
  }
}""",

"listServiceOrdersByStatus": """query listServiceOrdersByStatus($status: String!) {
  listServiceOrdersByStatus(status: $status) {
    items
    totalCount
  }
}""",

"listServiceOrdersByDate": """query listServiceOrdersByDate($startDate: String!, $endDate: String!) {
  listServiceOrdersByDate(startDate: $startDate, endDate: $endDate) {
    items
    totalCount
  }
}""",

"getServiceOrder": """query getServiceOrder($id: String!) {
  getServiceOrder(id: $id) {
    id
    title
    description
    status
    priority
    technicianId
    scheduledDate
  }
}""",

"getServiceOrdersByTechnician": """query getServiceOrdersByTechnician($technicianId: String!) {
  getServiceOrdersByTechnician(technicianId: $technicianId) {
    items
    totalCount
  }
}""",

"listComplianceByStatus": """query listComplianceByStatus($status: String!) {
  listComplianceByStatus(status: $status) {
    items
    totalCount
  }
}""",

"getCompliance": """query getCompliance($id: String!) {
  getCompliance(id: $id) {
    id
    firmwareId
    firmwareVersion
    deviceModel
    status
    certifications
    vulnerabilities
  }
}""",

"getComplianceByCertification": """query getComplianceByCertification($certification: String!) {
  getComplianceByCertification(certification: $certification) {
    items
    totalCount
  }
}""",

"listAuditLogs": """query listAuditLogs($startDate: String!, $endDate: String!, $limit: Int) {
  listAuditLogs(startDate: $startDate, endDate: $endDate, limit: $limit) {
    items
    nextToken
    totalCount
  }
}""",

"getAuditLogsByUser": """query getAuditLogsByUser($userId: String!) {
  getAuditLogsByUser(userId: $userId) {
    items
    totalCount
  }
}""",

"getUserByEmail": """query getUserByEmail($email: String!) {
  getUserByEmail(email: $email) {
    id
    email
    role
    firstName
    lastName
  }
}""",

"listUsersByRole": """query listUsersByRole($role: String!) {
  listUsersByRole(role: $role) {
    items
    totalCount
  }
}""",

"getCustomerWithRelations": """query getCustomerWithRelations($customerId: String!) {
  getCustomerWithRelations(customerId: $customerId) {
    items
    totalCount
  }
}""",

# ── Mutations ────────────────────────────────────────────────────────────────
"createDevice": """mutation createDevice(
  $deviceName: String!
  $serialNumber: String!
  $model: String!
  $location: String!
  $status: String!
  $firmwareVersion: String!
  $customerId: String!
  $manufacturer: String
  $firmwareId: String
  $healthScore: Int
) {
  createDevice(
    deviceName: $deviceName
    serialNumber: $serialNumber
    model: $model
    location: $location
    status: $status
    firmwareVersion: $firmwareVersion
    customerId: $customerId
    manufacturer: $manufacturer
    firmwareId: $firmwareId
    healthScore: $healthScore
  ) {
    id
    deviceName
    serialNumber
    status
  }
}""",

"updateDeviceCoords": """mutation updateDeviceCoords(
  $address: String!
  $lat: Float!
  $lng: Float!
  $city: String!
  $country: String!
  $formattedAddress: String
) {
  updateDeviceCoords(
    address: $address
    lat: $lat
    lng: $lng
    city: $city
    country: $country
    formattedAddress: $formattedAddress
  ) {
    address
    lat
    lng
    city
    country
    formattedAddress
    cachedAt
    expiresAt
  }
}""",

"createFirmware": """mutation createFirmware(
  $name: String!
  $version: String!
  $deviceModel: String!
  $releaseDate: String!
  $fileName: String!
  $fileSize: Int!
  $fileSizeFormatted: String!
  $checksum: String!
  $uploadedBy: String!
  $s3Key: String!
  $s3Bucket: String!
  $manufacturer: String
  $checksumAlgorithm: String
  $releaseNotes: String
) {
  createFirmware(
    name: $name
    version: $version
    deviceModel: $deviceModel
    releaseDate: $releaseDate
    fileName: $fileName
    fileSize: $fileSize
    fileSizeFormatted: $fileSizeFormatted
    checksum: $checksum
    uploadedBy: $uploadedBy
    s3Key: $s3Key
    s3Bucket: $s3Bucket
    manufacturer: $manufacturer
    checksumAlgorithm: $checksumAlgorithm
    releaseNotes: $releaseNotes
  ) {
    id
    name
    version
    deviceModel
    status
  }
}""",

"approveFirmware": """mutation approveFirmware($firmwareId: String!, $approvedBy: String!) {
  approveFirmware(firmwareId: $firmwareId, approvedBy: $approvedBy) {
    id
    name
    status
  }
}""",

"createServiceOrder": """mutation createServiceOrder(
  $title: String!
  $technicianId: String!
  $technicianName: String!
  $serviceType: String!
  $location: String!
  $scheduledDate: String!
  $scheduledTime: String!
  $priority: String!
  $customerId: String!
  $createdBy: String!
  $description: String
  $deviceIds: String
) {
  createServiceOrder(
    title: $title
    technicianId: $technicianId
    technicianName: $technicianName
    serviceType: $serviceType
    location: $location
    scheduledDate: $scheduledDate
    scheduledTime: $scheduledTime
    priority: $priority
    customerId: $customerId
    createdBy: $createdBy
    description: $description
    deviceIds: $deviceIds
  ) {
    id
    title
    priority
    technicianId
  }
}""",

"createCompliance": """mutation createCompliance(
  $firmwareId: String!
  $firmwareVersion: String!
  $deviceModel: String!
  $submittedBy: String!
  $submittedByName: String!
  $certifications: [String]!
  $vulnerabilities: AWSJSON
  $totalVulnerabilities: Int
  $complianceNotes: String
) {
  createCompliance(
    firmwareId: $firmwareId
    firmwareVersion: $firmwareVersion
    deviceModel: $deviceModel
    submittedBy: $submittedBy
    submittedByName: $submittedByName
    certifications: $certifications
    vulnerabilities: $vulnerabilities
    totalVulnerabilities: $totalVulnerabilities
    complianceNotes: $complianceNotes
  ) {
    id
    firmwareId
    certifications
  }
}""",

"updateEntityStatus": """mutation updateEntityStatus(
  $entityType: String!
  $id: String!
  $newStatus: String!
  $updatedBy: String
) {
  updateEntityStatus(
    entityType: $entityType
    id: $id
    newStatus: $newStatus
    updatedBy: $updatedBy
  )
}""",
}  # end Q dict


# ═════════════════════════════════════════════════════════════════════════════
# Build OpenAPI spec
# ═════════════════════════════════════════════════════════════════════════════
spec = {
    "openapi": "3.0.3",
    "info": {
        "title":       "HLM Platform API",
        "version":     "1.0.0",
        "description": (
            "Complete API specification for the HLM (Hardware Lifecycle Management) Platform.\n\n"
            "## Architecture\n"
            "All application data calls are **GraphQL POST** requests to the single AWS AppSync endpoint.\n"
            "Map tiles are standard REST GET requests via AWS Location Service.\n\n"
            "## Authentication\n"
            "All GraphQL requests require a raw Cognito **IdToken** in the `Authorization` header "
            "(no `Bearer` prefix). Obtain the token by calling the Cognito `InitiateAuth` endpoint first.\n\n"
            f"- **User Pool:** `{POOL_ID}`\n"
            f"- **Client ID:** `{CLIENT_ID}`\n\n"
            "## Live Test Results (2026-03-27)\n"
            "All 20 queries and 7 mutations were executed against the live AppSync endpoint "
            "and passed. New bugs discovered are marked ⚠️ in the relevant operation descriptions.\n\n"
            "## Important Notes\n"
            "- `items` in `PaginatedResponse` is an `AWSJSON` scalar — it cannot be sub-selected in GraphQL; parse it as JSON on the client\n"
            "- `totalCount` — not `total`\n"
            "- `operationName` in the POST body **must exactly match** the named operation in the `query` string"
        ),
        "contact": {"name": "HLM QA Team", "email": "ajaykumar.yadav@3pillarglobal.com"},
    },
    "servers": [
        {"url": GRAPHQL_URL,         "description": "AWS AppSync GraphQL Endpoint"},
        {"url": MAP_BASE,             "description": "AWS Location Service (Map Tiles)"},
        {"url": COGNITO_URL.rstrip("/"), "description": "AWS Cognito IDP (Auth)"},
    ],
    "tags": [
        {"name": "Auth",            "description": "Cognito authentication — obtain IdToken"},
        {"name": "Device Queries",  "description": "listDevices · getDevice · getDevicesByCustomer · getDevicesByLocation"},
        {"name": "Firmware Queries","description": "listFirmware · getFirmware · getFirmwareByModel · getFirmwareWithRelations"},
        {"name": "ServiceOrder Queries",  "description": "listServiceOrdersByStatus · listServiceOrdersByDate · getServiceOrder · getServiceOrdersByTechnician"},
        {"name": "Compliance Queries",    "description": "listComplianceByStatus · getCompliance · getComplianceByCertification"},
        {"name": "AuditLog Queries",      "description": "listAuditLogs · getAuditLogsByUser"},
        {"name": "User Queries",          "description": "getUserByEmail · listUsersByRole · getCustomerWithRelations"},
        {"name": "Device Mutations",      "description": "createDevice · updateDeviceCoords"},
        {"name": "Firmware Mutations",    "description": "createFirmware · approveFirmware"},
        {"name": "ServiceOrder Mutations","description": "createServiceOrder"},
        {"name": "Compliance Mutations",  "description": "createCompliance"},
        {"name": "Status Mutations",      "description": "updateEntityStatus (cross-entity)"},
        {"name": "Map Tiles (REST)",      "description": "AWS Location Service — GET endpoints used by MapLibre on the Geo Location tab"},
    ],

    # ── Security schemes ──────────────────────────────────────────────────────
    "components": {
        "securitySchemes": {
            "CognitoIdToken": {
                "type":        "apiKey",
                "in":          "header",
                "name":        "Authorization",
                "description": (
                    "Raw Cognito **IdToken** — no `Bearer` prefix.\n\n"
                    "Obtain by calling `POST /auth/login` (Cognito InitiateAuth).\n"
                    "Token expires in 1 hour."
                )
            }
        },
        "responses": {
            "Unauthorized": {
                "description": "401 — Missing or expired Authorization token",
                "content": {"application/json": {"example": {"errors": [{"message": "UnauthorizedException"}]}}}
            },
            "BadRequest": {
                "description": "400 — GraphQL validation error or missing required argument",
                "content": {"application/json": {"example": {"errors": [{"message": "Validation error of type MissingFieldArgument"}]}}}
            }
        },
        "schemas": {
            # ── shared GraphQL envelope ──
            "GraphQLRequest": {
                "type": "object",
                "required": ["operationName", "query", "variables"],
                "properties": {
                    "operationName": {"type": "string", "description": "Must exactly match the named operation in `query`"},
                    "query":         {"type": "string", "description": "Full GraphQL query or mutation document"},
                    "variables":     {"type": "object", "description": "Variable values — see each operation's example"}
                }
            },
            "GraphQLResponse": {
                "type": "object",
                "properties": {
                    "data":   {"type": "object", "nullable": True},
                    "errors": {"type": "array", "items": {"type": "object"},
                               "description": "Present only when the operation failed"}
                }
            },
            # ── entity types ──
            "PaginatedResponse": {
                "type": "object",
                "description": "⚠️ `items` is an AWSJSON scalar — parse it as a JSON array on the client side",
                "properties": {
                    "items":      {"type": "string", "description": "Serialised JSON array of domain objects (AWSJSON scalar)"},
                    "nextToken":  {"type": "string", "nullable": True, "description": "Cursor for next page, null if no more pages"},
                    "totalCount": {"type": "integer"}
                }
            },
            "DeviceType": {
                "type": "object",
                "properties": {
                    "id":              {"type": "string", "format": "uuid"},
                    "deviceName":      {"type": "string"},
                    "serialNumber":    {"type": "string"},
                    "model":           {"type": "string"},
                    "status":          {"type": "string", "enum": ["Online", "Offline", "Maintenance"]},
                    "location":        {"type": "string"},
                    "lat":             {"type": "number"},
                    "lng":             {"type": "number"},
                    "customerId":      {"type": "string"},
                    "firmwareVersion": {"type": "string"}
                }
            },
            "FirmwareType": {
                "type": "object",
                "properties": {
                    "id":          {"type": "string", "format": "uuid"},
                    "name":        {"type": "string"},
                    "version":     {"type": "string"},
                    "deviceModel": {"type": "string"},
                    "status":      {"type": "string", "enum": ["Pending", "Approved", "Rejected", "Deprecated"]},
                    "releaseDate": {"type": "string", "format": "date"},
                    "fileSize":    {"type": "integer", "description": "File size in bytes (Int, not Float)"},
                    "checksum":    {"type": "string"}
                }
            },
            "ServiceOrderType": {
                "type": "object",
                "properties": {
                    "id":            {"type": "string", "format": "uuid"},
                    "title":         {"type": "string"},
                    "description":   {"type": "string"},
                    "status":        {"type": "string", "description": "⚠️ Selecting this field in createServiceOrder response causes an enum serialization error"},
                    "technicianId":  {"type": "string"},
                    "technicianName":{"type": "string"},
                    "serviceType":   {"type": "string"},
                    "location":      {"type": "string"},
                    "scheduledDate": {"type": "string", "format": "date"},
                    "scheduledTime": {"type": "string"},
                    "priority":      {"type": "string", "enum": ["Low", "Medium", "High", "Critical"]},
                    "customerId":    {"type": "string"},
                    "createdBy":     {"type": "string"}
                }
            },
            "ComplianceType": {
                "type": "object",
                "properties": {
                    "id":              {"type": "string", "format": "uuid"},
                    "firmwareId":      {"type": "string"},
                    "firmwareVersion": {"type": "string"},
                    "deviceModel":     {"type": "string"},
                    "status":          {"type": "string", "enum": ["Approved", "Pending", "Deprecated"]},
                    "certifications":  {"type": "array", "items": {"type": "string"},
                                        "description": "⚠️ Introspection incorrectly shows String! — must pass [String]! array"},
                    "vulnerabilities": {"type": "integer"}
                }
            },
            "AuditLogType": {
                "type": "object",
                "properties": {
                    "id":           {"type": "string"},
                    "userId":       {"type": "string"},
                    "userEmail":    {"type": "string"},
                    "action":       {"type": "string"},
                    "auditStatus":  {"type": "string"},
                    "resourceType": {"type": "string"},
                    "resourceId":   {"type": "string"}
                }
            },
            "UserType": {
                "type": "object",
                "properties": {
                    "id":        {"type": "string"},
                    "email":     {"type": "string", "format": "email"},
                    "role":      {"type": "string", "enum": ["Admin", "Manager", "Technician"]},
                    "firstName": {"type": "string"},
                    "lastName":  {"type": "string"}
                }
            },
            "GeocodedDeviceType": {
                "type": "object",
                "description": "⚠️ API catalogue incorrectly listed an 'id' field — this type has NO id field",
                "properties": {
                    "address":          {"type": "string"},
                    "lat":              {"type": "number"},
                    "lng":              {"type": "number"},
                    "city":             {"type": "string"},
                    "country":          {"type": "string"},
                    "formattedAddress": {"type": "string", "nullable": True},
                    "cachedAt":         {"type": "string"},
                    "expiresAt":        {"type": "string"}
                }
            },
        }
    },

    # ── Paths ─────────────────────────────────────────────────────────────────
    "paths": {}
}

P = spec["paths"]

# ─── Auth ────────────────────────────────────────────────────────────────────
P["/auth/login"] = {
    "post": {
        "operationId": "cognitoLogin",
        "summary":     "Login — Get Cognito IdToken",
        "description": (
            "Authenticates with AWS Cognito and returns an `IdToken`.\n\n"
            f"**Endpoint:** `{COGNITO_URL}` (not the GraphQL server)\n\n"
            "Copy the `AuthenticationResult.IdToken` value and use it as the raw `Authorization` "
            "header value on every GraphQL request — **no `Bearer` prefix**.\n\n"
            f"Client ID: `{CLIENT_ID}` · Pool ID: `{POOL_ID}`"
        ),
        "tags": ["Auth"],
        "servers": [{"url": COGNITO_URL.rstrip("/"), "description": "AWS Cognito IDP"}],
        "requestBody": {
            "required": True,
            "content": {
                "application/json": {
                    "schema": {
                        "type": "object",
                        "required": ["AuthFlow", "ClientId", "AuthParameters"],
                        "properties": {
                            "AuthFlow":       {"type": "string", "example": "USER_PASSWORD_AUTH"},
                            "ClientId":       {"type": "string", "example": CLIENT_ID},
                            "AuthParameters": {
                                "type": "object",
                                "properties": {
                                    "USERNAME": {"type": "string", "example": "ajaykumar.yadav@3pillarglobal.com"},
                                    "PASSWORD": {"type": "string", "example": "Secure@12345"}
                                }
                            }
                        }
                    },
                    "example": {
                        "AuthFlow": "USER_PASSWORD_AUTH",
                        "ClientId": CLIENT_ID,
                        "AuthParameters": {
                            "USERNAME": "ajaykumar.yadav@3pillarglobal.com",
                            "PASSWORD": "Secure@12345"
                        }
                    }
                }
            }
        },
        "parameters": [
            {"in": "header", "name": "Content-Type",  "required": True,
             "schema": {"type": "string", "example": "application/x-amz-json-1.1"}},
            {"in": "header", "name": "X-Amz-Target", "required": True,
             "schema": {"type": "string", "example": "AWSCognitoIdentityProviderService.InitiateAuth"}},
        ],
        "responses": {
            "200": {
                "description": "Authentication successful — copy IdToken into Authorization header",
                "content": {
                    "application/json": {
                        "example": {
                            "AuthenticationResult": {
                                "IdToken":      "<JWT — use this as Authorization header value>",
                                "AccessToken":  "<JWT>",
                                "RefreshToken": "<JWT>",
                                "ExpiresIn":    3600,
                                "TokenType":    "Bearer"
                            }
                        }
                    }
                }
            },
            "400": {"description": "Invalid credentials or missing parameters"}
        }
    }
}

# ─── Device Queries ──────────────────────────────────────────────────────────
P["/graphql/queries/listDevices"] = graphql_path(
    "listDevices", "post",
    "List devices (optional status filter)",
    "Returns all devices or filters by `status`.\n\n"
    "**Live results:** All=12, Online=6, Offline=3, Maintenance=3\n\n"
    "**Status values:** `Online` | `Offline` | `Maintenance`\n"
    "Omit `status` to return all records.",
    ["Device Queries"],
    Q["listDevices"], {}
)

P["/graphql/queries/listDevices/online"] = graphql_path(
    "listDevices", "post", "listDevices — Online filter",
    "Filter devices by Online status. Confirmed totalCount: 6.",
    ["Device Queries"], Q["listDevices"], {"status": "Online"}
)

P["/graphql/queries/listDevices/offline"] = graphql_path(
    "listDevices", "post", "listDevices — Offline filter",
    "Filter devices by Offline status. Confirmed totalCount: 3.",
    ["Device Queries"], Q["listDevices"], {"status": "Offline"}
)

P["/graphql/queries/listDevices/maintenance"] = graphql_path(
    "listDevices", "post", "listDevices — Maintenance filter",
    "Filter devices by Maintenance status. Confirmed totalCount: 3.",
    ["Device Queries"], Q["listDevices"], {"status": "Maintenance"}
)

P["/graphql/queries/getDevice"] = graphql_path(
    "getDevice", "post", "Get device by ID",
    "Fetch a single device record by its unique ID. Returns null for non-existent IDs.",
    ["Device Queries"], Q["getDevice"],
    {"id": "a11bc9b9-ba1e-4e1f-8523-45cd003a6845"}
)

P["/graphql/queries/getDevicesByCustomer"] = graphql_path(
    "getDevicesByCustomer", "post", "Get devices by customer",
    "All devices for a customer account.\n\n"
    "**Note:** Demo customer IDs are c001–c005.\n"
    "Live test: c001 returned totalCount=0 (no devices mapped in demo data).",
    ["Device Queries"], Q["getDevicesByCustomer"], {"customerId": "c001"}
)

P["/graphql/queries/getDevicesByLocation"] = graphql_path(
    "getDevicesByLocation", "post", "Get devices by location",
    "All devices at a named location. `location` must exactly match the string stored on the device record.",
    ["Device Queries"], Q["getDevicesByLocation"], {"location": "New York Office"}
)

# ─── Firmware Queries ────────────────────────────────────────────────────────
P["/graphql/queries/listFirmware"] = graphql_path(
    "listFirmware", "post", "List firmware (optional status filter)",
    "All firmware or filtered by status.\n\n"
    "**Live results:** All=9, Pending=2, Approved=2, Rejected=1\n\n"
    "**Status values:** `Pending` | `Approved` | `Rejected` | `Deprecated`",
    ["Firmware Queries"], Q["listFirmware"], {}
)

P["/graphql/queries/listFirmware/deprecated"] = graphql_path(
    "listFirmware", "post",
    "listFirmware — Deprecated filter ⚠️ BUG",
    "Passing `status: Deprecated` returns ALL firmware records — filter is not applied server-side.\n\n"
    "**Live test:** totalCount=4 returned (not just deprecated records).\n"
    "**Workaround:** Filter client-side after fetching all records.",
    ["Firmware Queries"], Q["listFirmware"], {"status": "Deprecated"},
    deprecated=True,
    warning="KNOWN BUG — Deprecated filter not applied server-side"
)

P["/graphql/queries/getFirmware"] = graphql_path(
    "getFirmware", "post", "Get firmware by ID",
    "Single firmware record. `fileSize` is `Int` (bytes, not Float).",
    ["Firmware Queries"], Q["getFirmware"],
    {"id": "94688ba5-81b0-4c73-9f2a-5923f0b4b256"}
)

P["/graphql/queries/getFirmwareByModel"] = graphql_path(
    "getFirmwareByModel", "post", "Get firmware by device model",
    "All firmware versions for a given device model. Confirmed: XR-5000 → totalCount=3.",
    ["Firmware Queries"], Q["getFirmwareByModel"], {"deviceModel": "XR-5000"}
)

P["/graphql/queries/getFirmwareWithRelations"] = graphql_path(
    "getFirmwareWithRelations", "post", "Get firmware with related records",
    "Firmware + related compliance records + audit log entries — all returned as nested AWSJSON inside `items`. Parse client-side.",
    ["Firmware Queries"], Q["getFirmwareWithRelations"],
    {"id": "94688ba5-81b0-4c73-9f2a-5923f0b4b256"}
)

# ─── Service Order Queries ───────────────────────────────────────────────────
P["/graphql/queries/listServiceOrdersByStatus"] = graphql_path(
    "listServiceOrdersByStatus", "post", "List service orders by status",
    "Filter service orders by workflow status.\n\n"
    "**Status values:** `Pending` | `In Progress` | `Completed` | `Cancelled`\n\n"
    "**Live results:** Pending=0, In Progress=0, Completed=2, Cancelled=1",
    ["ServiceOrder Queries"], Q["listServiceOrdersByStatus"], {"status": "Pending"}
)

P["/graphql/queries/listServiceOrdersByDate"] = graphql_path(
    "listServiceOrdersByDate", "post", "List service orders by date range",
    "Service orders within a date range (ISO 8601 strings).\n\n"
    "- Valid range → returns matching records (2025–2026 returned totalCount=9)\n"
    "- Future range → returns empty array (totalCount=0)\n"
    "- **Reversed range (end < start) → throws raw DynamoDB BETWEEN error (400)** ⚠️",
    ["ServiceOrder Queries"], Q["listServiceOrdersByDate"],
    {"startDate": "2025-01-01", "endDate": "2026-12-31"}
)

P["/graphql/queries/listServiceOrdersByDate/reversed"] = graphql_path(
    "listServiceOrdersByDate", "post",
    "listServiceOrdersByDate — Reversed range ⚠️ BUG",
    "**NEW BUG (found during live testing):** Reversed range (end < start) throws a raw DynamoDB error instead of returning an empty array.\n\n"
    "Error: `Invalid KeyConditionExpression: The BETWEEN operator requires upper bound >= lower bound`\n\n"
    "API catalogue incorrectly stated this returns an empty array.",
    ["ServiceOrder Queries"], Q["listServiceOrdersByDate"],
    {"startDate": "2026-12-31", "endDate": "2025-01-01"},
    deprecated=True,
    warning="NEW BUG — Reversed range throws DynamoDB 400 error"
)

P["/graphql/queries/getServiceOrder"] = graphql_path(
    "getServiceOrder", "post", "Get service order by ID",
    "Single service order record. Sample: 'Core Switch Firmware Upgrade' (Completed).",
    ["ServiceOrder Queries"], Q["getServiceOrder"],
    {"id": "acf1df53-cf21-476d-9410-251efbf04b37"}
)

P["/graphql/queries/getServiceOrdersByTechnician"] = graphql_path(
    "getServiceOrdersByTechnician", "post", "Get service orders by technician",
    "All service orders assigned to a technician. Confirmed: u003 (Carol) → totalCount=5.",
    ["ServiceOrder Queries"], Q["getServiceOrdersByTechnician"], {"technicianId": "u003"}
)

# ─── Compliance Queries ──────────────────────────────────────────────────────
P["/graphql/queries/listComplianceByStatus"] = graphql_path(
    "listComplianceByStatus", "post", "List compliance records by status",
    "Filter compliance records by certification status.\n\n"
    "**Status values:** `Approved` | `Pending` | `Deprecated`\n\n"
    "**Live results:** Approved=5, Pending=2, Deprecated=0",
    ["Compliance Queries"], Q["listComplianceByStatus"], {"status": "Approved"}
)

P["/graphql/queries/getCompliance"] = graphql_path(
    "getCompliance", "post", "Get compliance record by ID",
    "Single compliance record. `certifications` is returned as an array.",
    ["Compliance Queries"], Q["getCompliance"],
    {"id": "c1ef0b2f-0ef2-49e8-8aa9-86ca9ee21401"}
)

P["/graphql/queries/getComplianceByCertification"] = graphql_path(
    "getComplianceByCertification", "post", "Get compliance by certification",
    "Filter by certification standard.\n\n"
    "**Known certifications:** `HIPAA` | `ISO 27001` | `FCC` | `WiFi Alliance` | `CE`\n\n"
    "**Live results:** HIPAA=1, ISO 27001=2, FCC=1, CE=1\n"
    "Note: WiFi Alliance returns no `totalCount` field in response.",
    ["Compliance Queries"], Q["getComplianceByCertification"], {"certification": "HIPAA"}
)

# ─── Audit Log Queries ───────────────────────────────────────────────────────
P["/graphql/queries/listAuditLogs"] = graphql_path(
    "listAuditLogs", "post", "List audit logs by date range",
    "Audit log entries within a time window (ISO 8601 strings).\n\n"
    "- Dashboard uses 24-hour window\n"
    "- Deployment Audit Log uses `limit: 50`\n"
    "- Future date range → returns empty array\n\n"
    "**Live results:** Broad 2025–2026 range with limit=50 → totalCount=50",
    ["AuditLog Queries"], Q["listAuditLogs"],
    {"startDate": "2025-01-01", "endDate": "2026-12-31", "limit": 50}
)

P["/graphql/queries/getAuditLogsByUser"] = graphql_path(
    "getAuditLogsByUser", "post", "Get audit logs by user",
    "Audit trail for a specific user.\n\n"
    "**Live results:** u001 (Alice)=4, u002 (Bob)=2, u003 (Carol)=1",
    ["AuditLog Queries"], Q["getAuditLogsByUser"], {"userId": "u001"}
)

# ─── User & Customer Queries ─────────────────────────────────────────────────
P["/graphql/queries/getUserByEmail"] = graphql_path(
    "getUserByEmail", "post", "Get user by email",
    "Look up a user profile by email address. Called on every page load for session init.\n\n"
    "Returns `null` if the email exists in Cognito but not in the application DB.\n\n"
    "**Live results:**\n"
    "- alice@acmecorp.com → id: u001 ✅\n"
    "- carol@acmecorp.com → id: u003 ✅\n"
    "- bob@acmecorp.com → **null** ⚠️ (Bob exists in Cognito but not in app DB)",
    ["User Queries"], Q["getUserByEmail"], {"email": "alice@acmecorp.com"}
)

P["/graphql/queries/listUsersByRole"] = graphql_path(
    "listUsersByRole", "post",
    "List users by role ⚠️ BUG",
    "**KNOWN BUG CONFIRMED:** `role` filter is NOT applied server-side.\n\n"
    "Passing any value — including `GARBAGE_ROLE` — returns ALL users (totalCount=5).\n\n"
    "**Workaround:** Filter client-side after fetching all records.",
    ["User Queries"], Q["listUsersByRole"], {"role": "Technician"},
    deprecated=False,
    warning="KNOWN BUG — role filter not applied server-side"
)

P["/graphql/queries/getCustomerWithRelations"] = graphql_path(
    "getCustomerWithRelations", "post", "Get customer with related devices and orders",
    "Customer record + all related devices + service orders, nested inside `items` AWSJSON. Parse client-side.\n\n"
    "Demo customer IDs: `c001` – `c005`",
    ["User Queries"], Q["getCustomerWithRelations"], {"customerId": "c001"}
)

# ─── Device Mutations ────────────────────────────────────────────────────────
P["/graphql/mutations/createDevice"] = graphql_path(
    "createDevice", "post", "Create a new device",
    "Register a new device. New devices default to Online status.\n\n"
    "**Required fields confirmed via live testing (not in original docs):**\n"
    "`location`, `status`, `firmwareVersion`, `customerId`\n\n"
    "**Live test:** PASS — id: `dc912136-7b56-4b8e-98ea-ec01a3b201bf`",
    ["Device Mutations"], Q["createDevice"],
    {
        "deviceName": "QA-TEST-Device-01", "serialNumber": "SN-QA-001",
        "model": "XR-5000", "location": "QA Lab", "status": "Online",
        "firmwareVersion": "1.0.0", "customerId": "c001",
        "manufacturer": "Acme Corp", "healthScore": 85
    }
)

P["/graphql/mutations/updateDeviceCoords"] = graphql_path(
    "updateDeviceCoords", "post", "Update device GPS coordinates",
    "Update GPS coordinates for a device.\n\n"
    "**Required fields confirmed via live testing (not in original docs):**\n"
    "`city`, `country`\n\n"
    "**⚠️ Return type correction:** `GeocodedDeviceType` has NO `id` field — API catalogue was wrong.\n"
    "Return fields: `address`, `lat`, `lng`, `city`, `country`, `formattedAddress`, `cachedAt`, `expiresAt`\n\n"
    "**Live test:** PASS — lat=40.7484, lng=-73.9967",
    ["Device Mutations"], Q["updateDeviceCoords"],
    {"address": "350 Fifth Avenue, New York, NY 10118",
     "lat": 40.7484, "lng": -73.9967, "city": "New York", "country": "USA",
     "formattedAddress": "350 Fifth Ave, New York, NY 10118, USA"}
)

# ─── Firmware Mutations ──────────────────────────────────────────────────────
P["/graphql/mutations/createFirmware"] = graphql_path(
    "createFirmware", "post", "Upload and register new firmware",
    "Register a new firmware package. Defaults to `Pending` status.\n\n"
    "**Required fields confirmed via live testing (not in original docs):**\n"
    "`releaseDate`, `fileName`, `fileSizeFormatted`, `uploadedBy`, `s3Key`, `s3Bucket`\n\n"
    "**Note:** `fileSize` is `Int` (bytes), not Float.\n\n"
    "**Live test:** PASS — id: `adbfccd8-7f36-4b1c-a35f-0eed4a7b623c`, status: Pending",
    ["Firmware Mutations"], Q["createFirmware"],
    {
        "name": "QA-TEST Firmware v9.9", "version": "9.9.0", "deviceModel": "XR-5000",
        "releaseDate": "2026-03-27", "fileName": "qa-fw-9.9.0.bin",
        "fileSize": 1024000, "fileSizeFormatted": "1000 KB",
        "checksum": "sha256:abc123", "uploadedBy": "u001",
        "s3Key": "firmware/qa-fw-9.9.0.bin", "s3Bucket": "hlm-firmware-bucket",
        "checksumAlgorithm": "SHA-256", "releaseNotes": "QA test build"
    }
)

P["/graphql/mutations/approveFirmware"] = graphql_path(
    "approveFirmware", "post", "Approve firmware (Pending → Approved)",
    "Approve a firmware package for deployment.\n\n"
    "**Required fields confirmed via live testing (not in original docs):**\n"
    "Parameter is `firmwareId` (not `id`) + `approvedBy`\n\n"
    "**Live test:** PASS — firmware `94688ba5` changed to Approved",
    ["Firmware Mutations"], Q["approveFirmware"],
    {"firmwareId": "94688ba5-81b0-4c73-9f2a-5923f0b4b256", "approvedBy": "u001"}
)

# ─── Service Order Mutations ─────────────────────────────────────────────────
P["/graphql/mutations/createServiceOrder"] = graphql_path(
    "createServiceOrder", "post", "Create a new service order",
    "Create a service / maintenance ticket. New orders default to `Pending` status.\n\n"
    "**Required fields confirmed via live testing (not in original docs):**\n"
    "`technicianId`, `technicianName`, `serviceType`, `location`, `scheduledDate`,\n"
    "`scheduledTime`, `priority`, `customerId`, `createdBy`\n\n"
    "**⚠️ Do NOT select `status` in response** — causes enum serialization error.\n\n"
    "**Live test:** PASS — id: `e6f719af-45af-4981-ac7f-e73221bc8bb4`",
    ["ServiceOrder Mutations"], Q["createServiceOrder"],
    {
        "title": "QA-TEST Service Order", "description": "Manual QA test",
        "technicianId": "u003", "technicianName": "Carol Smith",
        "serviceType": "Maintenance", "location": "QA Lab",
        "scheduledDate": "2026-04-01", "scheduledTime": "09:00",
        "priority": "Low", "customerId": "c001", "createdBy": "u001"
    }
)

# ─── Compliance Mutations ────────────────────────────────────────────────────
P["/graphql/mutations/createCompliance"] = graphql_path(
    "createCompliance", "post", "Submit firmware for compliance review",
    "Submit a firmware version for compliance certification review. Defaults to `Pending` status.\n\n"
    "**Required fields confirmed via live testing (not in original docs):**\n"
    "`submittedBy`, `submittedByName`\n\n"
    "**⚠️ Schema discrepancy:** Introspection shows `certifications` as `String!` but "
    "runtime requires `[String]!` (array). Always pass an array of strings.\n\n"
    "**Live test:** PASS — id: `cceeac67-44fa-48c1-b3b4-b1692e8e1099`, certs: [HIPAA, FCC]",
    ["Compliance Mutations"], Q["createCompliance"],
    {
        "firmwareId": "94688ba5-81b0-4c73-9f2a-5923f0b4b256",
        "firmwareVersion": "2.1.0", "deviceModel": "XR-5000",
        "submittedBy": "u001", "submittedByName": "Alice Turner",
        "certifications": ["HIPAA", "FCC"], "totalVulnerabilities": 0,
        "complianceNotes": "QA test submission"
    }
)

# ─── Status Mutations ────────────────────────────────────────────────────────
P["/graphql/mutations/updateEntityStatus"] = graphql_path(
    "updateEntityStatus", "post", "Update entity status (cross-entity)",
    "Generic status-transition mutation for firmware and compliance records.\n\n"
    "**entityType values:** `firmware` | `compliance`\n\n"
    "Returns raw AWSJSON of the updated DynamoDB record.",
    ["Status Mutations"], Q["updateEntityStatus"],
    {"entityType": "firmware",
     "id": "94688ba5-81b0-4c73-9f2a-5923f0b4b256",
     "newStatus": "Approved", "updatedBy": "u001"}
)

P["/graphql/mutations/updateEntityStatus/bug-noValidation"] = graphql_path(
    "updateEntityStatus", "post",
    "updateEntityStatus — No status validation ⚠️ BUG",
    "**KNOWN BUG CONFIRMED:** No server-side enum validation on `newStatus`.\n\n"
    "Passing `GARBAGE_VALUE` returns HTTP 200 and the value is persisted in DynamoDB.\n\n"
    "**Live test:** `status: GARBAGE_VALUE` was written successfully.",
    ["Status Mutations"], Q["updateEntityStatus"],
    {"entityType": "firmware", "id": "98910328-e3cb-4d03-8690-5212c30ccece",
     "newStatus": "GARBAGE_VALUE"},
    deprecated=True,
    warning="KNOWN BUG — Any string accepted as newStatus"
)

P["/graphql/mutations/updateEntityStatus/bug-upsert"] = graphql_path(
    "updateEntityStatus", "post",
    "updateEntityStatus — Non-existent ID upsert ⚠️ BUG",
    "**KNOWN BUG CONFIRMED:** No existence check on `id`.\n\n"
    "Non-existent UUID returns HTTP 200 and creates a phantom record (upsert behaviour).\n\n"
    "**Live test:** `00000000-0000-0000-0000-000000000000` was created with status `Approved`.",
    ["Status Mutations"], Q["updateEntityStatus"],
    {"entityType": "firmware", "id": "00000000-0000-0000-0000-000000000000",
     "newStatus": "Approved"},
    deprecated=True,
    warning="KNOWN BUG — Non-existent ID creates phantom record"
)

# ─── REST Map Tiles ──────────────────────────────────────────────────────────
P[f"/maps/v0/maps/{MAP_NAME}/style-descriptor"] = {
    "get": {
        "operationId": "getMapStyleDescriptor",
        "summary":     "Get map style descriptor",
        "description": "Fetch map style JSON (colours, layers, fonts). Called once on map init by MapLibre SDK.\n\nAuth: AWS Signature v4 via Amplify session credentials (added automatically by the SDK).",
        "tags":        ["Map Tiles (REST)"],
        "servers":     [{"url": MAP_BASE, "description": "AWS Location Service"}],
        "responses":   {"200": {"description": "Style JSON"}, "403": {"description": "Invalid AWS credentials"}}
    }
}

P[f"/maps/v0/maps/{MAP_NAME}/sprites/sprites@2x.png"] = {
    "get": {
        "operationId": "getMapSpritePng",
        "summary":     "Get map icon sprite sheet (PNG)",
        "description": "Icon sprite sheet image for map symbols. Auth: AWS Signature v4.",
        "tags":        ["Map Tiles (REST)"],
        "servers":     [{"url": MAP_BASE, "description": "AWS Location Service"}],
        "responses":   {"200": {"description": "PNG image"}}
    }
}

P[f"/maps/v0/maps/{MAP_NAME}/sprites/sprites@2x.json"] = {
    "get": {
        "operationId": "getMapSpriteJson",
        "summary":     "Get map icon sprite metadata (JSON)",
        "description": "Icon sprite coordinate metadata. Used alongside the sprite PNG. Auth: AWS Signature v4.",
        "tags":        ["Map Tiles (REST)"],
        "servers":     [{"url": MAP_BASE, "description": "AWS Location Service"}],
        "responses":   {"200": {"description": "Sprite metadata JSON"}}
    }
}

P[f"/maps/v0/maps/{MAP_NAME}/tiles/{{z}}/{{x}}/{{y}}"] = {
    "get": {
        "operationId": "getMapTile",
        "summary":     "Get vector map tile",
        "description": "Vector map tile at z/x/y coordinates. Loaded by MapLibre on every pan/zoom. Auth: AWS Signature v4.",
        "tags":        ["Map Tiles (REST)"],
        "servers":     [{"url": MAP_BASE, "description": "AWS Location Service"}],
        "parameters":  [
            {"name": "z", "in": "path", "required": True, "description": "Zoom level (e.g. 7)", "schema": {"type": "integer", "example": 7}},
            {"name": "x", "in": "path", "required": True, "description": "Tile column", "schema": {"type": "integer", "example": 35}},
            {"name": "y", "in": "path", "required": True, "description": "Tile row", "schema": {"type": "integer", "example": 48}},
        ],
        "responses": {"200": {"description": "Vector tile data"}}
    }
}

# ─── Write file ───────────────────────────────────────────────────────────────
class LiteralString(str): pass

def literal_representer(dumper, data):
    if "\n" in data:
        return dumper.represent_scalar("tag:yaml.org,2002:str", data, style="|")
    return dumper.represent_scalar("tag:yaml.org,2002:str", data)

yaml.add_representer(LiteralString, literal_representer)
yaml.add_representer(str, literal_representer)

with open(OUTPUT, "w", encoding="utf-8") as f:
    yaml.dump(spec, f, allow_unicode=True, sort_keys=False,
              default_flow_style=False, width=120)

total_paths = len(spec["paths"])
print(f"✅  Saved: {OUTPUT}")
print(f"   Paths  : {total_paths}")
print(f"   Queries: 20  Mutations: 9 paths (7 ops + 2 bug cases)  Auth: 1  REST: 4")
print(f"   Schemas: {len(spec['components']['schemas'])} entity types defined")
print()
print(f"   Import into Swagger UI:")
print(f"   1. Go to https://editor.swagger.io")
print(f"   2. File → Import File → select HLM-API-Swagger.yaml")
print(f"   OR host locally:")
print(f"   npx @redocly/cli preview-docs HLM-API-Swagger.yaml")
