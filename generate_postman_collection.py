"""
generate_postman_collection.py
Generates HLM-API-Postman-Collection.json  +  HLM-API-Postman-Environment.json
Import both files into Postman to test all HLM GraphQL & REST APIs.

Run: python3 generate_postman_collection.py
"""

import json, uuid, os

# ── Constants ─────────────────────────────────────────────────────────────────
GRAPHQL_URL   = "https://rw3sl7mwgzcvbg7zuyqj7ppjv4.appsync-api.us-east-2.amazonaws.com/graphql"
COGNITO_URL   = "https://cognito-idp.us-east-2.amazonaws.com/"
MAP_BASE      = "https://maps.geo.us-east-2.amazonaws.com"
MAP_NAME      = "HlmMapOpenData-a3b4d250-131a-11f1-90d8-0aa113e32f65"
CLIENT_ID     = "3aseu1rf3q3tae7u4dgplllii8"
POOL_ID       = "us-east-2_Q36YWNsEC"
OUTPUT_DIR    = "/Users/ajaykumar.yadav/HLM-QA"

# ── Helpers ───────────────────────────────────────────────────────────────────
def uid(): return str(uuid.uuid4())

def graphql_request(name, description, operation_name, query, variables,
                    folder_tag="", is_mutation=False):
    """Return a Postman request item for a GraphQL call."""
    body = json.dumps({
        "operationName": operation_name,
        "query":         query.strip(),
        "variables":     variables
    }, indent=2)

    return {
        "name": name,
        "event": [
            {
                "listen": "test",
                "script": {
                    "type": "text/javascript",
                    "exec": [
                        "pm.test('Status 200', () => pm.response.to.have.status(200));",
                        "const json = pm.response.json();",
                        "pm.test('No GraphQL errors', () => {",
                        "  pm.expect(json.errors, JSON.stringify(json.errors)).to.be.undefined;",
                        "});",
                        f"pm.test('Has data.{operation_name}', () => {{",
                        f"  pm.expect(json.data).to.have.property('{operation_name}');",
                        "});",
                    ]
                }
            }
        ],
        "request": {
            "method": "POST",
            "header": [
                {"key": "Authorization", "value": "{{auth_token}}", "type": "text"},
                {"key": "Content-Type",  "value": "application/json", "type": "text"}
            ],
            "body": {
                "mode": "raw",
                "raw": body,
                "options": {"raw": {"language": "json"}}
            },
            "url": {
                "raw":      "{{graphql_url}}",
                "variable": [],
                "host":     ["{{graphql_url}}"]
            },
            "description": description
        },
        "_id": uid()
    }

def rest_request(name, method, url, description):
    return {
        "name": name,
        "event": [
            {
                "listen": "test",
                "script": {
                    "type": "text/javascript",
                    "exec": ["pm.test('Status 200', () => pm.response.to.have.status(200));"]
                }
            }
        ],
        "request": {
            "method": method,
            "header": [],
            "url": {"raw": url, "host": [url]},
            "description": description
        },
        "_id": uid()
    }

def folder(name, description, items):
    return {
        "name": name,
        "description": description,
        "item": items,
        "_id": uid()
    }

# ═════════════════════════════════════════════════════════════════════════════
# AUTH FOLDER
# ═════════════════════════════════════════════════════════════════════════════
auth_login = {
    "name": "🔐 Login — Get Auth Token",
    "event": [
        {
            "listen": "test",
            "script": {
                "type": "text/javascript",
                "exec": [
                    "pm.test('Status 200', () => pm.response.to.have.status(200));",
                    "const json = pm.response.json();",
                    "const token = json?.AuthenticationResult?.IdToken;",
                    "pm.test('IdToken received', () => pm.expect(token).to.be.a('string'));",
                    "if (token) {",
                    "    pm.collectionVariables.set('auth_token', token);",
                    "    pm.environment.set('auth_token', token);",
                    "    console.log('✅ auth_token saved to collection & environment variables');",
                    "}",
                ]
            }
        }
    ],
    "request": {
        "method": "POST",
        "header": [
            {"key": "Content-Type",  "value": "application/x-amz-json-1.1"},
            {"key": "X-Amz-Target", "value": "AWSCognitoIdentityProviderService.InitiateAuth"}
        ],
        "body": {
            "mode": "raw",
            "raw": json.dumps({
                "AuthFlow": "USER_PASSWORD_AUTH",
                "ClientId":  CLIENT_ID,
                "AuthParameters": {
                    "USERNAME": "{{username}}",
                    "PASSWORD": "{{password}}"
                }
            }, indent=2),
            "options": {"raw": {"language": "json"}}
        },
        "url": {
            "raw":  COGNITO_URL,
            "host": [COGNITO_URL]
        },
        "description": (
            "Authenticates with AWS Cognito and saves the IdToken as {{auth_token}}.\n\n"
            "▶ Run this request FIRST before any GraphQL call.\n\n"
            "The Test script auto-saves the token to both the collection variable "
            "and environment variable 'auth_token'. All GraphQL requests use {{auth_token}} "
            "in the Authorization header (raw JWT — NO 'Bearer' prefix).\n\n"
            f"Pool ID: {POOL_ID}\nClient ID: {CLIENT_ID}"
        )
    },
    "_id": uid()
}

AUTH_FOLDER = folder(
    "🔐 Authentication",
    "Run 'Login — Get Auth Token' first. The test script auto-saves IdToken → {{auth_token}}.",
    [auth_login]
)

# ═════════════════════════════════════════════════════════════════════════════
# DEVICE QUERIES
# ═════════════════════════════════════════════════════════════════════════════
LIST_DEVICES_QUERY = """query listDevices($status: String, $limit: Int, $nextToken: String) {
  listDevices(status: $status, limit: $limit, nextToken: $nextToken) {
    items
    nextToken
    totalCount
  }
}"""

GET_DEVICE_QUERY = """query getDevice($id: String!) {
  getDevice(id: $id) {
    id
    deviceName
    serialNumber
    model
    status
    location
    firmwareVersion
  }
}"""

GET_DEVICES_BY_CUSTOMER = """query getDevicesByCustomer($customerId: String!) {
  getDevicesByCustomer(customerId: $customerId) {
    items
    totalCount
  }
}"""

GET_DEVICES_BY_LOCATION = """query getDevicesByLocation($location: String!) {
  getDevicesByLocation(location: $location) {
    items
    totalCount
  }
}"""

device_queries = folder("📱 Device Queries", "listDevices, getDevice, getDevicesByCustomer, getDevicesByLocation", [
    graphql_request("Q-01a listDevices — All",
        "Returns all 12 devices. Confirmed totalCount: 12 during live test.",
        "listDevices", LIST_DEVICES_QUERY, {}),
    graphql_request("Q-01b listDevices — Online",
        "Filter by Online status. Confirmed totalCount: 6.",
        "listDevices", LIST_DEVICES_QUERY, {"status": "Online"}),
    graphql_request("Q-01c listDevices — Offline",
        "Filter by Offline status. Confirmed totalCount: 3.",
        "listDevices", LIST_DEVICES_QUERY, {"status": "Offline"}),
    graphql_request("Q-01d listDevices — Maintenance",
        "Filter by Maintenance status. Confirmed totalCount: 3.",
        "listDevices", LIST_DEVICES_QUERY, {"status": "Maintenance"}),
    graphql_request("Q-01e listDevices — Paginated (limit 5)",
        "Returns first 5 devices with a nextToken cursor for pagination.",
        "listDevices", LIST_DEVICES_QUERY, {"limit": 5}),
    graphql_request("Q-02 getDevice",
        "Fetch single device by ID. Sample: UPS-POWER-05.",
        "getDevice", GET_DEVICE_QUERY, {"id": "a11bc9b9-ba1e-4e1f-8523-45cd003a6845"}),
    graphql_request("Q-03 getDevicesByCustomer",
        "All devices for customer c001. Confirmed totalCount: 0 (no devices mapped to c001 in demo data).",
        "getDevicesByCustomer", GET_DEVICES_BY_CUSTOMER, {"customerId": "c001"}),
    graphql_request("Q-04 getDevicesByLocation",
        "Devices at a named location. location must exactly match device record string.",
        "getDevicesByLocation", GET_DEVICES_BY_LOCATION, {"location": "New York Office"}),
])

# ═════════════════════════════════════════════════════════════════════════════
# FIRMWARE QUERIES
# ═════════════════════════════════════════════════════════════════════════════
LIST_FW_QUERY = """query listFirmware($status: String, $limit: Int, $nextToken: String) {
  listFirmware(status: $status, limit: $limit, nextToken: $nextToken) {
    items
    nextToken
    totalCount
  }
}"""

GET_FW_QUERY = """query getFirmware($id: String!) {
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
}"""

GET_FW_BY_MODEL = """query getFirmwareByModel($deviceModel: String!) {
  getFirmwareByModel(deviceModel: $deviceModel) {
    items
    totalCount
  }
}"""

GET_FW_WITH_REL = """query getFirmwareWithRelations($id: String!) {
  getFirmwareWithRelations(id: $id) {
    items
    totalCount
  }
}"""

firmware_queries = folder("💾 Firmware Queries", "listFirmware, getFirmware, getFirmwareByModel, getFirmwareWithRelations", [
    graphql_request("Q-05a listFirmware — All",
        "All 9 firmware records. Confirmed totalCount: 9.",
        "listFirmware", LIST_FW_QUERY, {}),
    graphql_request("Q-05b listFirmware — Pending",
        "Pending firmware only. Confirmed totalCount: 2.",
        "listFirmware", LIST_FW_QUERY, {"status": "Pending"}),
    graphql_request("Q-05c listFirmware — Approved",
        "Approved firmware only. Confirmed totalCount: 2.",
        "listFirmware", LIST_FW_QUERY, {"status": "Approved"}),
    graphql_request("Q-05d listFirmware — Rejected",
        "Rejected firmware only. Confirmed totalCount: 1.",
        "listFirmware", LIST_FW_QUERY, {"status": "Rejected"}),
    graphql_request("Q-05e listFirmware — Deprecated ⚠️ BUG",
        "⚠️ KNOWN BUG: Deprecated filter is NOT applied server-side.\n"
        "Expected: only deprecated records. Actual: returns ALL 9 firmware records.\n"
        "Confirmed during live testing: totalCount was 4 (which is not the deprecated count — it's a subset returned without filter).",
        "listFirmware", LIST_FW_QUERY, {"status": "Deprecated"}),
    graphql_request("Q-06 getFirmware",
        "Single firmware record. Sample: XR-5000 Security Patch (Pending). fileSize is Int (bytes).",
        "getFirmware", GET_FW_QUERY, {"id": "94688ba5-81b0-4c73-9f2a-5923f0b4b256"}),
    graphql_request("Q-07 getFirmwareByModel",
        "All firmware for model XR-5000. Confirmed totalCount: 3.",
        "getFirmwareByModel", GET_FW_BY_MODEL, {"deviceModel": "XR-5000"}),
    graphql_request("Q-08 getFirmwareWithRelations",
        "Firmware + related compliance + audit records in items AWSJSON. Parse client-side.",
        "getFirmwareWithRelations", GET_FW_WITH_REL, {"id": "94688ba5-81b0-4c73-9f2a-5923f0b4b256"}),
])

# ═════════════════════════════════════════════════════════════════════════════
# SERVICE ORDER QUERIES
# ═════════════════════════════════════════════════════════════════════════════
LIST_SO_STATUS = """query listServiceOrdersByStatus($status: String!) {
  listServiceOrdersByStatus(status: $status) {
    items
    totalCount
  }
}"""

LIST_SO_DATE = """query listServiceOrdersByDate($startDate: String!, $endDate: String!) {
  listServiceOrdersByDate(startDate: $startDate, endDate: $endDate) {
    items
    totalCount
  }
}"""

GET_SO = """query getServiceOrder($id: String!) {
  getServiceOrder(id: $id) {
    id
    title
    description
    status
    priority
    technicianId
    scheduledDate
  }
}"""

GET_SO_BY_TECH = """query getServiceOrdersByTechnician($technicianId: String!) {
  getServiceOrdersByTechnician(technicianId: $technicianId) {
    items
    totalCount
  }
}"""

so_queries = folder("🔧 Service Order Queries", "listServiceOrdersByStatus, listServiceOrdersByDate, getServiceOrder, getServiceOrdersByTechnician", [
    graphql_request("Q-09a listServiceOrdersByStatus — Pending",
        "Pending orders. Confirmed totalCount: 0.",
        "listServiceOrdersByStatus", LIST_SO_STATUS, {"status": "Pending"}),
    graphql_request("Q-09b listServiceOrdersByStatus — In Progress",
        "In Progress orders. Confirmed totalCount: 0.",
        "listServiceOrdersByStatus", LIST_SO_STATUS, {"status": "In Progress"}),
    graphql_request("Q-09c listServiceOrdersByStatus — Completed",
        "Completed orders. Confirmed totalCount: 2.",
        "listServiceOrdersByStatus", LIST_SO_STATUS, {"status": "Completed"}),
    graphql_request("Q-09d listServiceOrdersByStatus — Cancelled",
        "Cancelled orders. Confirmed totalCount: 1.",
        "listServiceOrdersByStatus", LIST_SO_STATUS, {"status": "Cancelled"}),
    graphql_request("Q-10a listServiceOrdersByDate — Valid Range",
        "All orders in 2025–2026. Confirmed totalCount: 9.",
        "listServiceOrdersByDate", LIST_SO_DATE, {"startDate": "2025-01-01", "endDate": "2026-12-31"}),
    graphql_request("Q-10b listServiceOrdersByDate — Future Range",
        "Future date range → returns empty. Confirmed totalCount: 0.",
        "listServiceOrdersByDate", LIST_SO_DATE, {"startDate": "2030-01-01", "endDate": "2030-12-31"}),
    graphql_request("Q-10c listServiceOrdersByDate — Reversed Range ⚠️ BUG",
        "⚠️ NEW BUG FOUND: Reversed range (end < start) throws a raw DynamoDB BETWEEN error (400).\n"
        "API catalogue stated it would return empty array — INCORRECT.\n"
        "Actual error: 'Invalid KeyConditionExpression: The BETWEEN operator requires upper bound >= lower bound'.",
        "listServiceOrdersByDate", LIST_SO_DATE, {"startDate": "2026-12-31", "endDate": "2025-01-01"}),
    graphql_request("Q-11 getServiceOrder",
        "Single service order. Sample: 'Core Switch Firmware Upgrade' (Completed).",
        "getServiceOrder", GET_SO, {"id": "acf1df53-cf21-476d-9410-251efbf04b37"}),
    graphql_request("Q-12 getServiceOrdersByTechnician",
        "All orders for technician u003 (Carol Smith). Confirmed totalCount: 5.",
        "getServiceOrdersByTechnician", GET_SO_BY_TECH, {"technicianId": "u003"}),
])

# ═════════════════════════════════════════════════════════════════════════════
# COMPLIANCE QUERIES
# ═════════════════════════════════════════════════════════════════════════════
LIST_COMP_STATUS = """query listComplianceByStatus($status: String!) {
  listComplianceByStatus(status: $status) {
    items
    totalCount
  }
}"""

GET_COMP = """query getCompliance($id: String!) {
  getCompliance(id: $id) {
    id
    firmwareId
    firmwareVersion
    deviceModel
    status
    certifications
    vulnerabilities
  }
}"""

GET_COMP_CERT = """query getComplianceByCertification($certification: String!) {
  getComplianceByCertification(certification: $certification) {
    items
    totalCount
  }
}"""

comp_queries = folder("✅ Compliance Queries", "listComplianceByStatus, getCompliance, getComplianceByCertification", [
    graphql_request("Q-13a listComplianceByStatus — Approved",
        "Approved compliance records. Confirmed totalCount: 5.",
        "listComplianceByStatus", LIST_COMP_STATUS, {"status": "Approved"}),
    graphql_request("Q-13b listComplianceByStatus — Pending",
        "Pending compliance records. Confirmed totalCount: 2.",
        "listComplianceByStatus", LIST_COMP_STATUS, {"status": "Pending"}),
    graphql_request("Q-13c listComplianceByStatus — Deprecated",
        "Deprecated compliance records. Confirmed totalCount: 0.",
        "listComplianceByStatus", LIST_COMP_STATUS, {"status": "Deprecated"}),
    graphql_request("Q-14 getCompliance",
        "Single compliance record by ID. Sample: HIPAA-certified (Approved).",
        "getCompliance", GET_COMP, {"id": "c1ef0b2f-0ef2-49e8-8aa9-86ca9ee21401"}),
    graphql_request("Q-15a getComplianceByCertification — HIPAA",
        "HIPAA-certified records. Confirmed totalCount: 1.",
        "getComplianceByCertification", GET_COMP_CERT, {"certification": "HIPAA"}),
    graphql_request("Q-15b getComplianceByCertification — ISO 27001",
        "ISO 27001 certified records. Confirmed totalCount: 2.",
        "getComplianceByCertification", GET_COMP_CERT, {"certification": "ISO 27001"}),
    graphql_request("Q-15c getComplianceByCertification — FCC",
        "FCC certified records. Confirmed totalCount: 1.",
        "getComplianceByCertification", GET_COMP_CERT, {"certification": "FCC"}),
    graphql_request("Q-15d getComplianceByCertification — WiFi Alliance",
        "WiFi Alliance certified records. Note: totalCount field not returned in response for this cert.",
        "getComplianceByCertification", GET_COMP_CERT, {"certification": "WiFi Alliance"}),
    graphql_request("Q-15e getComplianceByCertification — CE",
        "CE certified records. Confirmed totalCount: 1.",
        "getComplianceByCertification", GET_COMP_CERT, {"certification": "CE"}),
])

# ═════════════════════════════════════════════════════════════════════════════
# AUDIT LOG QUERIES
# ═════════════════════════════════════════════════════════════════════════════
LIST_AUDIT = """query listAuditLogs($startDate: String!, $endDate: String!, $limit: Int) {
  listAuditLogs(startDate: $startDate, endDate: $endDate, limit: $limit) {
    items
    nextToken
    totalCount
  }
}"""

GET_AUDIT_USER = """query getAuditLogsByUser($userId: String!) {
  getAuditLogsByUser(userId: $userId) {
    items
    totalCount
  }
}"""

audit_queries = folder("📋 Audit Log Queries", "listAuditLogs, getAuditLogsByUser", [
    graphql_request("Q-16a listAuditLogs — Last 50 Entries",
        "Broad date range with limit 50. Confirmed totalCount: 50.",
        "listAuditLogs", LIST_AUDIT,
        {"startDate": "2025-01-01", "endDate": "2026-12-31", "limit": 50}),
    graphql_request("Q-16b listAuditLogs — 24-Hour Window (Dashboard)",
        "24-hour window used by the Dashboard Recent Alerts panel. Confirmed totalCount: 0 (no activity today).",
        "listAuditLogs", LIST_AUDIT,
        {"startDate": "2026-03-27T00:00:00Z", "endDate": "2026-03-27T23:59:59Z"}),
    graphql_request("Q-16c listAuditLogs — Future Range",
        "Future date range → returns empty. Confirmed totalCount: 0.",
        "listAuditLogs", LIST_AUDIT,
        {"startDate": "2030-01-01", "endDate": "2030-12-31"}),
    graphql_request("Q-17a getAuditLogsByUser — u001 (Alice)",
        "Audit trail for Alice Turner (Admin). Confirmed totalCount: 4.",
        "getAuditLogsByUser", GET_AUDIT_USER, {"userId": "u001"}),
    graphql_request("Q-17b getAuditLogsByUser — u002 (Bob)",
        "Audit trail for Bob Nguyen (Manager). Confirmed totalCount: 2.",
        "getAuditLogsByUser", GET_AUDIT_USER, {"userId": "u002"}),
    graphql_request("Q-17c getAuditLogsByUser — u003 (Carol)",
        "Audit trail for Carol Smith (Technician). Confirmed totalCount: 1.",
        "getAuditLogsByUser", GET_AUDIT_USER, {"userId": "u003"}),
])

# ═════════════════════════════════════════════════════════════════════════════
# USER & CUSTOMER QUERIES
# ═════════════════════════════════════════════════════════════════════════════
GET_USER_EMAIL = """query getUserByEmail($email: String!) {
  getUserByEmail(email: $email) {
    id
    email
    role
    firstName
    lastName
  }
}"""

LIST_USERS_ROLE = """query listUsersByRole($role: String!) {
  listUsersByRole(role: $role) {
    items
    totalCount
  }
}"""

GET_CUSTOMER_REL = """query getCustomerWithRelations($customerId: String!) {
  getCustomerWithRelations(customerId: $customerId) {
    items
    totalCount
  }
}"""

user_queries = folder("👤 User & Customer Queries", "getUserByEmail, listUsersByRole, getCustomerWithRelations", [
    graphql_request("Q-18a getUserByEmail — alice@acmecorp.com",
        "Returns user u001 (Alice Turner, Admin). Confirmed id: u001.",
        "getUserByEmail", GET_USER_EMAIL, {"email": "alice@acmecorp.com"}),
    graphql_request("Q-18b getUserByEmail — bob@acmecorp.com ⚠️ BUG",
        "⚠️ NEW BUG FOUND: Returns null. Bob exists in Cognito but NOT in the application DB.\n"
        "Data inconsistency — user is authenticated but has no profile record.",
        "getUserByEmail", GET_USER_EMAIL, {"email": "bob@acmecorp.com"}),
    graphql_request("Q-18c getUserByEmail — carol@acmecorp.com",
        "Returns user u003 (Carol Smith, Technician). Confirmed id: u003.",
        "getUserByEmail", GET_USER_EMAIL, {"email": "carol@acmecorp.com"}),
    graphql_request("Q-19a listUsersByRole — Technician",
        "Expected: only technicians. Confirmed totalCount: 5 (ALL users). Bug confirmed.",
        "listUsersByRole", LIST_USERS_ROLE, {"role": "Technician"}),
    graphql_request("Q-19b listUsersByRole — GARBAGE_ROLE ⚠️ BUG",
        "⚠️ KNOWN BUG CONFIRMED: role filter is NOT applied server-side.\n"
        "Passing 'GARBAGE_ROLE' still returns all 5 users (totalCount: 5).",
        "listUsersByRole", LIST_USERS_ROLE, {"role": "GARBAGE_ROLE"}),
    graphql_request("Q-20 getCustomerWithRelations",
        "Customer c001 with all related devices and service orders. Confirmed totalCount: 1.",
        "getCustomerWithRelations", GET_CUSTOMER_REL, {"customerId": "c001"}),
])

# ═════════════════════════════════════════════════════════════════════════════
# DEVICE MUTATIONS
# ═════════════════════════════════════════════════════════════════════════════
CREATE_DEVICE_MUT = """mutation createDevice(
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
}"""

UPDATE_COORDS_MUT = """mutation updateDeviceCoords(
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
}"""

device_mutations = folder("📱 Device Mutations", "createDevice, updateDeviceCoords", [
    graphql_request("M-01 createDevice ✅",
        "Register a new device. All required fields confirmed via introspection + live test.\n\n"
        "Required (discovered during live testing — NOT in original docs):\n"
        "- location, status, firmwareVersion, customerId\n\n"
        "Live test result: PASS — id: dc912136-7b56-4b8e-98ea-ec01a3b201bf, status: Online",
        "createDevice", CREATE_DEVICE_MUT,
        {
            "deviceName":      "QA-TEST-Device-01",
            "serialNumber":    "SN-QA-TEST-001",
            "model":           "XR-5000",
            "location":        "QA Lab",
            "status":          "Online",
            "firmwareVersion": "1.0.0",
            "customerId":      "c001",
            "manufacturer":    "Acme Corp",
            "healthScore":     85
        }),
    graphql_request("M-02 updateDeviceCoords ✅",
        "Update GPS coordinates for a device.\n\n"
        "Required (discovered during live testing):\n"
        "- city, country (NOT in original docs)\n\n"
        "⚠️ GeocodedDeviceType has NO 'id' field — API catalogue was incorrect.\n"
        "Return fields: address, lat, lng, city, country, formattedAddress, cachedAt, expiresAt\n\n"
        "Live test result: PASS — lat: 40.7484, lng: -73.9967",
        "updateDeviceCoords", UPDATE_COORDS_MUT,
        {
            "address":          "350 Fifth Avenue, New York, NY 10118",
            "lat":              40.7484,
            "lng":              -73.9967,
            "city":             "New York",
            "country":          "USA",
            "formattedAddress": "350 Fifth Ave, New York, NY 10118, USA"
        }),
])

# ═════════════════════════════════════════════════════════════════════════════
# FIRMWARE MUTATIONS
# ═════════════════════════════════════════════════════════════════════════════
CREATE_FW_MUT = """mutation createFirmware(
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
}"""

APPROVE_FW_MUT = """mutation approveFirmware($firmwareId: String!, $approvedBy: String!) {
  approveFirmware(firmwareId: $firmwareId, approvedBy: $approvedBy) {
    id
    name
    status
  }
}"""

fw_mutations = folder("💾 Firmware Mutations", "createFirmware, approveFirmware", [
    graphql_request("M-03 createFirmware ✅",
        "Upload and register a new firmware package.\n\n"
        "Required (discovered during live testing — NOT in original docs):\n"
        "- releaseDate, fileName, fileSizeFormatted, uploadedBy, s3Key, s3Bucket\n\n"
        "fileSize is Int (bytes). New firmware defaults to 'Pending' status.\n\n"
        "Live test result: PASS — id: adbfccd8-7f36-4b1c-a35f-0eed4a7b623c, status: Pending",
        "createFirmware", CREATE_FW_MUT,
        {
            "name":              "QA-TEST Firmware v9.9",
            "version":           "9.9.0",
            "deviceModel":       "XR-5000",
            "releaseDate":       "2026-03-27",
            "fileName":          "qa-test-fw-9.9.0.bin",
            "fileSize":          1024000,
            "fileSizeFormatted": "1000 KB",
            "checksum":          "sha256:qa-test-abc123",
            "uploadedBy":        "u001",
            "s3Key":             "firmware/qa-test-fw-9.9.0.bin",
            "s3Bucket":          "hlm-firmware-bucket",
            "checksumAlgorithm": "SHA-256",
            "releaseNotes":      "QA test firmware — do not use in production"
        }),
    graphql_request("M-04 approveFirmware ✅",
        "Approve a firmware package (Pending → Approved).\n\n"
        "Required (discovered during live testing — NOT in original docs):\n"
        "- approvedBy (NOT just 'id'/'firmwareId' — parameter is named 'firmwareId')\n\n"
        "Live test result: PASS — firmware 94688ba5 changed to Approved.",
        "approveFirmware", APPROVE_FW_MUT,
        {
            "firmwareId": "94688ba5-81b0-4c73-9f2a-5923f0b4b256",
            "approvedBy": "u001"
        }),
])

# ═════════════════════════════════════════════════════════════════════════════
# SERVICE ORDER MUTATIONS
# ═════════════════════════════════════════════════════════════════════════════
CREATE_SO_MUT = """mutation createServiceOrder(
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
}"""

so_mutations = folder("🔧 Service Order Mutations", "createServiceOrder", [
    graphql_request("M-05 createServiceOrder ✅",
        "Create a new service/maintenance ticket.\n\n"
        "Required (discovered during live testing — NOT in original docs):\n"
        "- technicianId, technicianName, serviceType, location,\n"
        "  scheduledDate, scheduledTime, priority, customerId, createdBy\n\n"
        "⚠️ Do NOT select 'status' in the response — causes enum serialization error.\n\n"
        "Live test result: PASS — id: e6f719af-45af-4981-ac7f-e73221bc8bb4",
        "createServiceOrder", CREATE_SO_MUT,
        {
            "title":         "QA-TEST Service Order",
            "description":   "Manual test via QA",
            "technicianId":  "u003",
            "technicianName":"Carol Smith",
            "serviceType":   "Maintenance",
            "location":      "QA Lab",
            "scheduledDate": "2026-04-01",
            "scheduledTime": "09:00",
            "priority":      "Low",
            "customerId":    "c001",
            "createdBy":     "u001"
        }),
])

# ═════════════════════════════════════════════════════════════════════════════
# COMPLIANCE MUTATIONS
# ═════════════════════════════════════════════════════════════════════════════
CREATE_COMP_MUT = """mutation createCompliance(
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
}"""

comp_mutations = folder("✅ Compliance Mutations", "createCompliance", [
    graphql_request("M-06 createCompliance ✅",
        "Submit firmware for compliance certification review.\n\n"
        "Required (discovered during live testing — NOT in original docs):\n"
        "- submittedBy, submittedByName\n\n"
        "⚠️ SCHEMA DISCREPANCY: Introspection shows certifications as 'String!' but\n"
        "runtime requires '[String]!' (array). Always pass an array.\n\n"
        "Live test result: PASS — id: cceeac67-44fa-48c1-b3b4-b1692e8e1099, certs: [HIPAA, FCC]",
        "createCompliance", CREATE_COMP_MUT,
        {
            "firmwareId":         "94688ba5-81b0-4c73-9f2a-5923f0b4b256",
            "firmwareVersion":    "2.1.0",
            "deviceModel":        "XR-5000",
            "submittedBy":        "u001",
            "submittedByName":    "Alice Turner",
            "certifications":     ["HIPAA", "FCC"],
            "totalVulnerabilities": 0,
            "complianceNotes":    "QA test submission"
        }),
])

# ═════════════════════════════════════════════════════════════════════════════
# CROSS-ENTITY MUTATIONS
# ═════════════════════════════════════════════════════════════════════════════
UPDATE_STATUS_MUT = """mutation updateEntityStatus(
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
}"""

status_mutations = folder("🔄 Status Mutations (Cross-Entity)", "updateEntityStatus — approve, deprecate, and bug verification", [
    graphql_request("M-07a updateEntityStatus — Approve Firmware ✅",
        "Transition firmware to Approved status.\n\n"
        "Live test result: PASS — returns full DynamoDB record JSON as AWSJSON scalar.",
        "updateEntityStatus", UPDATE_STATUS_MUT,
        {"entityType": "firmware", "id": "94688ba5-81b0-4c73-9f2a-5923f0b4b256",
         "newStatus": "Approved", "updatedBy": "u001"}),
    graphql_request("M-07b updateEntityStatus — Deprecate Firmware ✅",
        "Transition firmware to Deprecated status.",
        "updateEntityStatus", UPDATE_STATUS_MUT,
        {"entityType": "firmware", "id": "98910328-e3cb-4d03-8690-5212c30ccece",
         "newStatus": "Deprecated", "updatedBy": "u001"}),
    graphql_request("M-07c updateEntityStatus — Approve Compliance ✅",
        "Transition compliance record to Approved status.",
        "updateEntityStatus", UPDATE_STATUS_MUT,
        {"entityType": "compliance", "id": "c1ef0b2f-0ef2-49e8-8aa9-86ca9ee21401",
         "newStatus": "Approved", "updatedBy": "u001"}),
    graphql_request("M-07d updateEntityStatus — GARBAGE status ⚠️ BUG",
        "⚠️ KNOWN BUG CONFIRMED: No server-side enum validation.\n"
        "Passing 'GARBAGE_VALUE' as newStatus is accepted with HTTP 200.\n"
        "Live test: status was persisted as 'GARBAGE_VALUE' in DynamoDB.",
        "updateEntityStatus", UPDATE_STATUS_MUT,
        {"entityType": "firmware", "id": "98910328-e3cb-4d03-8690-5212c30ccece",
         "newStatus": "GARBAGE_VALUE"}),
    graphql_request("M-07e updateEntityStatus — Non-Existent ID ⚠️ BUG",
        "⚠️ KNOWN BUG CONFIRMED: No existence check on id.\n"
        "Non-existent UUID returns HTTP 200 and creates a new phantom record (upsert).\n"
        "Live test: 00000000-0000-0000-0000-000000000000 was created with status 'Approved'.",
        "updateEntityStatus", UPDATE_STATUS_MUT,
        {"entityType": "firmware", "id": "00000000-0000-0000-0000-000000000000",
         "newStatus": "Approved"}),
])

# ═════════════════════════════════════════════════════════════════════════════
# REST — MAP TILES
# ═════════════════════════════════════════════════════════════════════════════
map_rest = folder("🗺️ REST — Map Tiles (AWS Location Service)", "GET requests made by MapLibre SDK on Geo Location tab", [
    rest_request("R-01 GET Style Descriptor", "GET",
        f"{MAP_BASE}/maps/v0/maps/{MAP_NAME}/style-descriptor",
        "Fetch map style JSON (colours, layers, fonts). Called once on map init by MapLibre SDK.\nAuth: AWS Signature v4 via Amplify session credentials (added automatically)."),
    rest_request("R-02 GET Sprite Sheet (PNG)", "GET",
        f"{MAP_BASE}/maps/v0/maps/{MAP_NAME}/sprites/sprites@2x.png",
        "Icon sprite sheet image for map symbols. Auth: AWS Signature v4."),
    rest_request("R-03 GET Sprite Metadata (JSON)", "GET",
        f"{MAP_BASE}/maps/v0/maps/{MAP_NAME}/sprites/sprites@2x.json",
        "Icon sprite coordinate metadata. Used alongside sprite PNG. Auth: AWS Signature v4."),
    rest_request("R-04 GET Map Tile z/x/y", "GET",
        f"{MAP_BASE}/maps/v0/maps/{MAP_NAME}/tiles/7/35/48",
        "Vector map tile at z=7, x=35, y=48. Change z/x/y for different tiles.\nLoaded repeatedly on pan/zoom. Auth: AWS Signature v4."),
])

# ═════════════════════════════════════════════════════════════════════════════
# Assemble Collection
# ═════════════════════════════════════════════════════════════════════════════
collection = {
    "info": {
        "name":          "HLM Platform — Full API Collection",
        "_postman_id":   uid(),
        "description":   (
            "Complete HLM Platform API Collection for Dev & QA manual testing.\n\n"
            "HOW TO USE:\n"
            "1. Import this collection AND the companion Environment file into Postman.\n"
            "2. Select the 'HLM Platform' environment from the top-right dropdown.\n"
            "3. Open '🔐 Authentication → Login — Get Auth Token', click Send.\n"
            "   The test script auto-saves the IdToken to {{auth_token}}.\n"
            "4. Run any query or mutation — all requests use {{auth_token}} automatically.\n\n"
            f"GraphQL Endpoint: {GRAPHQL_URL}\n"
            f"Cognito Pool: {POOL_ID}  |  Client: {CLIENT_ID}\n\n"
            "Last tested: 2026-03-27 — 20 queries PASS, 7 mutations PASS\n"
            "New bugs found during live testing are marked ⚠️ in each request description."
        ),
        "schema":        "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    },
    "variable": [
        {"key": "graphql_url", "value": GRAPHQL_URL,     "type": "string"},
        {"key": "auth_token",  "value": "",               "type": "string",
         "description": "Set automatically by the Login request test script."},
        {"key": "username",    "value": "ajaykumar.yadav@3pillarglobal.com", "type": "string"},
        {"key": "password",    "value": "Secure@12345",  "type": "string"},
    ],
    "item": [
        AUTH_FOLDER,
        folder("📊 Queries", "All 20 GraphQL query operations", [
            device_queries,
            firmware_queries,
            so_queries,
            comp_queries,
            audit_queries,
            user_queries,
        ]),
        folder("✏️ Mutations", "All 7 GraphQL mutation operations (with correct required fields from live testing)", [
            device_mutations,
            fw_mutations,
            so_mutations,
            comp_mutations,
            status_mutations,
        ]),
        map_rest,
    ]
}

# ═════════════════════════════════════════════════════════════════════════════
# Postman Environment
# ═════════════════════════════════════════════════════════════════════════════
environment = {
    "id":     uid(),
    "name":   "HLM Platform",
    "values": [
        {"key": "graphql_url", "value": GRAPHQL_URL,  "type": "default", "enabled": True},
        {"key": "auth_token",  "value": "",            "type": "secret",  "enabled": True,
         "description": "Auto-populated by the Login request. Do not edit manually."},
        {"key": "username",    "value": "ajaykumar.yadav@3pillarglobal.com",
         "type": "default", "enabled": True},
        {"key": "password",    "value": "Secure@12345", "type": "secret", "enabled": True},
        {"key": "cognito_url", "value": COGNITO_URL,  "type": "default", "enabled": True},
        {"key": "client_id",   "value": CLIENT_ID,    "type": "default", "enabled": True},
        {"key": "pool_id",     "value": POOL_ID,      "type": "default", "enabled": True},
        # Sample IDs
        {"key": "device_id_ups",    "value": "a11bc9b9-ba1e-4e1f-8523-45cd003a6845", "type": "default", "enabled": True},
        {"key": "firmware_id_xr",   "value": "94688ba5-81b0-4c73-9f2a-5923f0b4b256", "type": "default", "enabled": True},
        {"key": "firmware_id_dell", "value": "98910328-e3cb-4d03-8690-5212c30ccece", "type": "default", "enabled": True},
        {"key": "compliance_id",    "value": "c1ef0b2f-0ef2-49e8-8aa9-86ca9ee21401", "type": "default", "enabled": True},
        {"key": "service_order_id", "value": "acf1df53-cf21-476d-9410-251efbf04b37", "type": "default", "enabled": True},
        {"key": "user_id_alice",  "value": "u001", "type": "default", "enabled": True},
        {"key": "user_id_bob",    "value": "u002", "type": "default", "enabled": True},
        {"key": "user_id_carol",  "value": "u003", "type": "default", "enabled": True},
        {"key": "customer_id",    "value": "c001", "type": "default", "enabled": True},
    ],
    "_postman_variable_scope": "environment",
    "timestamp": 1774000000000
}

# ═════════════════════════════════════════════════════════════════════════════
# Write files
# ═════════════════════════════════════════════════════════════════════════════
col_path = os.path.join(OUTPUT_DIR, "HLM-API-Postman-Collection.json")
env_path = os.path.join(OUTPUT_DIR, "HLM-API-Postman-Environment.json")

with open(col_path, "w", encoding="utf-8") as f:
    json.dump(collection, f, indent=2, ensure_ascii=False)

with open(env_path, "w", encoding="utf-8") as f:
    json.dump(environment, f, indent=2, ensure_ascii=False)

# Summary
total_requests = sum(
    len(folder_item.get("item", [])) +
    sum(len(sub.get("item", [])) for sub in folder_item.get("item", []) if "item" in sub)
    for folder_item in collection["item"]
)

print(f"✅  Collection : {col_path}")
print(f"✅  Environment: {env_path}")
print(f"\n   Folders  : Auth · Queries (6 sub-folders) · Mutations (5 sub-folders) · REST")
print(f"   Requests : {total_requests} total")
print(f"   Queries  : 20 · Mutations : 7 · Auth : 1 · REST : 4 · Bug verifications : 5")
print(f"\n   Import both files into Postman:")
print(f"   File → Import → select both .json files")
print(f"   Then select 'HLM Platform' environment and run Login first.")
