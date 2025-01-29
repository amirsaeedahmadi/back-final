import http from 'k6/http';
import { sleep, check, group } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const BASE_URL = 'http://localhost:8083/v1';
const RAMP_UP_DURATION = '2m';
const STEADY_STATE_DURATION = '3m';
const MAX_VUS = 16;
const REQUEST_TIMEOUT = '5s';

const debugResponse = (name, response) => {
    console.log(`${name} status: ${response.status}`);
    console.log(`${name} body: ${response.body}`);
    if (response.status !== 200) {
        console.log(`${name} headers: ${JSON.stringify(response.headers)}`);
        console.log(`${name} request URL: ${response.request.url}`);
        console.log(`${name} request headers: ${JSON.stringify(response.request.headers)}`);
        console.log(`${name} request body: ${response.request.body}`);
    }
};

const USERS = {
    GOD: {
        email: "arabzadehamirhossein888@gmail.com",
        password: "securePassword123"
    },
    ADMIN: {
        email: "amir.arabzade888@gmail.com",
        password: "testpass123"
    },
    REGULAR: {
        email: "arabzadehamirhossein@outlook.com",
        password: "testpass123"
    }
};

export function setup() {
    console.log('Starting setup phase...');
    const tokens = {};

    for (const [role, credentials] of Object.entries(USERS)) {
        console.log(`Attempting login for ${role}...`);

        const loginResponse = http.post(`${BASE_URL}/auth/login`,
            JSON.stringify({
                email: credentials.email,
                password: credentials.password
            }),
            {
                headers: { 'Content-Type': 'application/json' },
                timeout: REQUEST_TIMEOUT,
                tags: { endpoint: 'login' }
            }
        );

        debugResponse(`${role} login`, loginResponse);

        try {
            if (loginResponse.status === 200) {
                const responseBody = JSON.parse(loginResponse.body);
                if (responseBody && responseBody.token) {
                    tokens[role] = responseBody.token;
                    console.log(`Successfully obtained token for ${role}`);
                } else {
                    console.error(`${role} login response missing token`);
                }
            } else {
                console.error(`${role} login failed with status ${loginResponse.status}`);
            }
        } catch (e) {
            console.error(`Error parsing ${role} login response: ${e.message}`);
        }

        check(loginResponse, {
            [`${role} login successful`]: (r) => r.status === 200,
            [`${role} response can be parsed`]: (r) => {
                try {
                    JSON.parse(r.body);
                    return true;
                } catch (e) {
                    return false;
                }
            }
        });

        sleep(1);
    }

    console.log('Setup phase completed');
    return { tokens };
}

export const options = {
    scenarios: {
        admin_endpoints: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: RAMP_UP_DURATION, target: 4 },
                { duration: STEADY_STATE_DURATION, target: 4 },
                { duration: '1m', target: 0 }
            ],
            exec: 'adminEndpointsScenario'
        },
        user_operations: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: RAMP_UP_DURATION, target: MAX_VUS },
                { duration: STEADY_STATE_DURATION, target: MAX_VUS },
                { duration: '1m', target: 0 }
            ],
            exec: 'userEndpointsScenario'
        },
        product_operations: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: RAMP_UP_DURATION, target: 8 },
                { duration: STEADY_STATE_DURATION, target: 8 },
                { duration: '1m', target: 0 }
            ],
            exec: 'productEndpointsScenario'
        }
    },
    thresholds: {
        'http_req_duration{endpoint:product}': ['p(95)<5000'],
        'http_req_duration{endpoint:report}': ['p(95)<5000'],
        'http_req_duration{endpoint:role}': ['p(95)<5000'],
        'http_req_failed{endpoint:product}': ['rate<0.1'],
        'http_req_failed{endpoint:report}': ['rate<0.1'],
        'http_req_failed{endpoint:role}': ['rate<0.1'],
        'http_req_duration': ['p(95)<5000'],
        'http_req_failed': ['rate<0.1']
    }
};

export function adminEndpointsScenario(data) {
    group('Admin Operations', () => {
        console.log('Starting admin operations...');

        const usersResponse = http.get(`${BASE_URL}/user/all`, {
            headers: {
                'Authorization': data.tokens.GOD
            },
            timeout: REQUEST_TIMEOUT,
            tags: { endpoint: 'users' }
        });

        debugResponse('Get users', usersResponse);

        check(usersResponse, {
            'get users status is 200': (r) => r.status === 200,
            'get users returns array': (r) => {
                try {
                    return Array.isArray(JSON.parse(r.body));
                } catch (e) {
                    console.error('Failed to parse users response:', e);
                    return false;
                }
            }
        });

        const targetUserId = 3;
        const newRole = "ADMIN";

        const headers = {
            'Authorization': data.tokens.GOD
        };

        const roleUpdateResponse = http.put(
            `${BASE_URL}/roles/update/${targetUserId}?newRole=${newRole}`,
            null,
            { headers }
        );

        console.log(`Role Update Response Status: ${roleUpdateResponse.status}`);

        debugResponse('Update role', roleUpdateResponse);

        check(roleUpdateResponse, {
            'role update status is 200': (r) => r.status === 200
        });

        sleep(randomIntBetween(1, 3));
    });
}

export function productEndpointsScenario(data) {
    group('Product Operations', () => {
        console.log('Starting product operations...');

        const productData = JSON.stringify({
            title: `Test Product ${Math.floor(Math.random() * 10000)}`,
            description: "Detailed product description",
            price: {
                amount: Math.floor(Math.random() * 1000) + 100,
                unit: "TOMAN"
            },
            category: "Electronics",
            productionYear: 2023,
            brand: "Test Brand",
            sellerId: 7
        });

        const boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        const body =
            `--${boundary}\r\n` +
            `Content-Disposition: form-data; name="product"\r\n\r\n` +
            `${productData}\r\n` +
            `--${boundary}--\r\n`;

        const headers = {
            'Authorization': data.tokens.REGULAR,
            'Content-Type': `multipart/form-data; boundary=${boundary}`
        };

        const response = http.post(`${BASE_URL}/product`, body, { headers });

        console.log(`Product Response Status: ${response.status}`);

        debugResponse('Create product', response);

        check(response, {
            'create product status is 200': (r) => r.status === 200
        });

        sleep(randomIntBetween(1, 3));

        const sellerProductsResponse = http.get(
            `${BASE_URL}/product/seller`,
            {
                headers: {
                    'Authorization': data.tokens.REGULAR
                },
                timeout: REQUEST_TIMEOUT,
                tags: { endpoint: 'product' }
            }
        );

        debugResponse('Get seller products', sellerProductsResponse);

        check(sellerProductsResponse, {
            'get seller products status is 200': (r) => r.status === 200,
            'returns products array': (r) => {
                try {
                    return Array.isArray(JSON.parse(r.body));
                } catch (e) {
                    console.error('Failed to parse seller products response:', e);
                    return false;
                }
            }
        });

        sleep(randomIntBetween(1, 3));
    });
}

export function userEndpointsScenario(data) {
    group('User Profile Operations', () => {
        console.log('Starting user profile operations...');

        const profileResponse = http.get(`${BASE_URL}/user/profile`, {
            headers: {
                'Authorization': data.tokens.REGULAR
            },
            timeout: REQUEST_TIMEOUT,
            tags: { endpoint: 'profile' }
        });

        debugResponse('Get profile', profileResponse);

        check(profileResponse, {
            'get profile status is 200': (r) => r.status === 200,
            'profile data valid': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body && typeof body.id !== 'undefined';
                } catch (e) {
                    console.error('Failed to parse profile response:', e);
                    return false;
                }
            }
        });

        const reportData = JSON.stringify({
            violationType: "INAPPROPRIATE_CONTENT",
            description: "Test report description",
            reportedContentId: 7
        });

        const boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        const body =
            `--${boundary}\r\n` +
            `Content-Disposition: form-data; name="report"\r\n\r\n` +
            `${reportData}\r\n` +
            `--${boundary}--\r\n`;

        const headers = {
            'Authorization': data.tokens.REGULAR,
            'Content-Type': `multipart/form-data; boundary=${boundary}`
        };

        const reportResponse = http.post(`${BASE_URL}/reports`, body, { headers });

        console.log(`Report Response Status: ${reportResponse.status}`);


        debugResponse('Create report', reportResponse);

        check(reportResponse, {
            'create report status is 200': (r) => r.status === 200
        });

        sleep(randomIntBetween(1, 3));
    });
}