import swaggerJsdoc from 'swagger-jsdoc';

const options = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'ClearSky Weather API',
      version: '1.0.0',
      description:
        'Backend API for the ClearSky Weather Android app. Provides weather forecasts, air quality, geocoding, alerts, historical data, radar imagery, and premium subscription management.',
    },
    servers: [
      {
        url: '/',
        description: 'Current server',
      },
    ],
    tags: [
      { name: 'Weather', description: 'Weather forecast endpoints' },
      { name: 'Air Quality', description: 'Air quality data' },
      { name: 'Geocoding', description: 'Location search' },
      { name: 'Alerts', description: 'Weather and air quality alerts, scheduler, and history' },
      { name: 'Historical', description: 'Historical weather data (Premium)' },
      { name: 'Radar', description: 'Radar and satellite imagery (Premium)' },
      { name: 'Premium', description: 'Premium subscription management' },
      { name: 'Devices', description: 'Push notification device management' },
      { name: 'Batch', description: 'Batch multi-location requests (v2 only)' },
      { name: 'API Keys', description: 'API key management for tiered rate limiting' },
      { name: 'System', description: 'Health and system status' },
    ],
    components: {
      securitySchemes: {
        BearerAuth: {
          type: 'http',
          scheme: 'bearer',
          description: 'Purchase token obtained via POST /api/premium/verify',
        },
      },
      schemas: {
        ErrorResponse: {
          type: 'object',
          properties: {
            success: { type: 'boolean', example: false },
            error: {
              type: 'object',
              properties: {
                code: { type: 'string', example: 'MISSING_PARAMETER' },
                message: { type: 'string', example: 'Both lat and lon query parameters are required' },
              },
            },
          },
        },
        Location: {
          type: 'object',
          properties: {
            lat: { type: 'number', example: 40.71 },
            lon: { type: 'number', example: -74.01 },
            name: { type: 'string', example: 'New York' },
          },
        },
      },
      parameters: {
        Latitude: {
          name: 'lat',
          in: 'query',
          required: true,
          schema: { type: 'number', minimum: -90, maximum: 90, example: 40.71 },
          description: 'Latitude (-90 to 90)',
        },
        Longitude: {
          name: 'lon',
          in: 'query',
          required: true,
          schema: { type: 'number', minimum: -180, maximum: 180, example: -74.01 },
          description: 'Longitude (-180 to 180)',
        },
        Units: {
          name: 'units',
          in: 'query',
          schema: { type: 'string', enum: ['metric', 'imperial'], default: 'metric' },
          description: 'Unit system',
        },
        Timezone: {
          name: 'timezone',
          in: 'query',
          schema: { type: 'string', default: 'auto' },
          description: 'Timezone (e.g., auto, America/New_York)',
        },
      },
    },
  },
  apis: ['./src/routes/*.js', './src/routes/v2/*.js', './src/app.js'],
};

export const swaggerSpec = swaggerJsdoc(options);
