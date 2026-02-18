import { getWeatherDescription, getWeatherIcon } from '../utils/weatherCodes.js';
import {
  convertTemperature,
  convertSpeed,
  convertPrecipitation,
  convertVisibility,
  convertArray,
  getUnitLabels,
} from '../utils/units.js';

function enrichWeatherCodes(codes, isDayArray) {
  if (!Array.isArray(codes)) return { descriptions: [], icons: [] };

  const descriptions = codes.map((code) => getWeatherDescription(code));
  const icons = codes.map((code, i) => {
    const isDay = Array.isArray(isDayArray) ? Boolean(isDayArray[i]) : true;
    return getWeatherIcon(code, isDay);
  });

  return { descriptions, icons };
}

function enrichDailyWeatherCodes(codes) {
  if (!Array.isArray(codes)) return { descriptions: [], icons: [] };

  const descriptions = codes.map((code) => getWeatherDescription(code));
  const icons = codes.map((code) => getWeatherIcon(code, true));
  return { descriptions, icons };
}

/**
 * Transform raw Open-Meteo forecast data into the client-facing shape.
 * @param {object} raw - Raw forecast response from Open-Meteo
 * @param {'metric' | 'imperial'} [units='metric']
 * @returns {object} Transformed forecast with location, current, hourly, daily, minutely15, and units
 */
export function transformForecast(raw, units = 'metric') {
  const conv = {
    temp: (v) => convertTemperature(v, units),
    speed: (v) => convertSpeed(v, units),
    precip: (v) => convertPrecipitation(v, units),
    vis: (v) => convertVisibility(v, units),
  };

  const currentIsDay = Boolean(raw.current?.is_day);

  const hourlyEnriched = enrichWeatherCodes(raw.hourly?.weather_code, raw.hourly?.is_day);

  const dailyEnriched = enrichDailyWeatherCodes(raw.daily?.weather_code);

  const minutely15Available = Boolean(raw.minutely_15?.time?.length);

  return {
    location: {
      latitude: raw.latitude,
      longitude: raw.longitude,
      elevation: raw.elevation,
      timezone: raw.timezone,
      timezoneAbbreviation: raw.timezone_abbreviation,
      utcOffsetSeconds: raw.utc_offset_seconds,
    },

    current: {
      time: raw.current?.time,
      temperature: conv.temp(raw.current?.temperature_2m),
      feelsLike: conv.temp(raw.current?.apparent_temperature),
      humidity: raw.current?.relative_humidity_2m,
      isDay: currentIsDay,
      precipitation: conv.precip(raw.current?.precipitation),
      rain: conv.precip(raw.current?.rain),
      showers: conv.precip(raw.current?.showers),
      snowfall: conv.precip(raw.current?.snowfall),
      weatherCode: raw.current?.weather_code,
      weatherDescription: getWeatherDescription(raw.current?.weather_code),
      weatherIcon: getWeatherIcon(raw.current?.weather_code, currentIsDay),
      cloudCover: raw.current?.cloud_cover,
      pressureMsl: raw.current?.pressure_msl,
      surfacePressure: raw.current?.surface_pressure,
      windSpeed: conv.speed(raw.current?.wind_speed_10m),
      windDirection: raw.current?.wind_direction_10m,
      windGusts: conv.speed(raw.current?.wind_gusts_10m),
    },

    hourly: {
      time: raw.hourly?.time,
      temperature: convertArray(raw.hourly?.temperature_2m, conv.temp),
      feelsLike: convertArray(raw.hourly?.apparent_temperature, conv.temp),
      humidity: raw.hourly?.relative_humidity_2m,
      precipitationProbability: raw.hourly?.precipitation_probability,
      precipitation: convertArray(raw.hourly?.precipitation, conv.precip),
      rain: convertArray(raw.hourly?.rain, conv.precip),
      snowfall: convertArray(raw.hourly?.snowfall, conv.precip),
      weatherCode: raw.hourly?.weather_code,
      weatherDescription: hourlyEnriched.descriptions,
      weatherIcon: hourlyEnriched.icons,
      cloudCover: raw.hourly?.cloud_cover,
      visibility: convertArray(raw.hourly?.visibility, conv.vis),
      windSpeed: convertArray(raw.hourly?.wind_speed_10m, conv.speed),
      windDirection: raw.hourly?.wind_direction_10m,
      windGusts: convertArray(raw.hourly?.wind_gusts_10m, conv.speed),
      uvIndex: raw.hourly?.uv_index,
      pressureMsl: raw.hourly?.pressure_msl,
      dewPoint: convertArray(raw.hourly?.dew_point_2m, conv.temp),
      isDay: raw.hourly?.is_day,
    },

    daily: {
      time: raw.daily?.time,
      temperatureMax: convertArray(raw.daily?.temperature_2m_max, conv.temp),
      temperatureMin: convertArray(raw.daily?.temperature_2m_min, conv.temp),
      feelsLikeMax: convertArray(raw.daily?.apparent_temperature_max, conv.temp),
      feelsLikeMin: convertArray(raw.daily?.apparent_temperature_min, conv.temp),
      precipitationSum: convertArray(raw.daily?.precipitation_sum, conv.precip),
      precipitationProbabilityMax: raw.daily?.precipitation_probability_max,
      rainSum: convertArray(raw.daily?.rain_sum, conv.precip),
      snowfallSum: convertArray(raw.daily?.snowfall_sum, conv.precip),
      weatherCode: raw.daily?.weather_code,
      weatherDescription: dailyEnriched.descriptions,
      weatherIcon: dailyEnriched.icons,
      sunrise: raw.daily?.sunrise,
      sunset: raw.daily?.sunset,
      sunshineDuration: raw.daily?.sunshine_duration,
      daylightDuration: raw.daily?.daylight_duration,
      uvIndexMax: raw.daily?.uv_index_max,
      windSpeedMax: convertArray(raw.daily?.wind_speed_10m_max, conv.speed),
      windGustsMax: convertArray(raw.daily?.wind_gusts_10m_max, conv.speed),
      windDirectionDominant: raw.daily?.wind_direction_10m_dominant,
      precipitationHours: raw.daily?.precipitation_hours,
    },

    minutely15: {
      available: minutely15Available,
      time: minutely15Available ? raw.minutely_15.time : [],
      precipitation: minutely15Available ? convertArray(raw.minutely_15.precipitation, conv.precip) : [],
      rain: minutely15Available ? convertArray(raw.minutely_15.rain, conv.precip) : [],
      snowfall: minutely15Available ? convertArray(raw.minutely_15.snowfall, conv.precip) : [],
      weatherCode: minutely15Available ? raw.minutely_15.weather_code : [],
    },

    units: getUnitLabels(units),
  };
}

function getAqiCategory(aqi) {
  if (aqi === null || aqi === undefined) return 'Unknown';
  if (aqi <= 50) return 'Good';
  if (aqi <= 100) return 'Moderate';
  if (aqi <= 150) return 'Unhealthy for Sensitive Groups';
  if (aqi <= 200) return 'Unhealthy';
  if (aqi <= 300) return 'Very Unhealthy';
  return 'Hazardous';
}

function hasPollenData(hourly) {
  const pollenFields = [
    'alder_pollen',
    'birch_pollen',
    'grass_pollen',
    'mugwort_pollen',
    'olive_pollen',
    'ragweed_pollen',
  ];
  return pollenFields.some((f) => Array.isArray(hourly?.[f]) && hourly[f].some((v) => v !== null && v !== undefined));
}

/**
 * Transform raw Open-Meteo air quality data into the client-facing shape.
 * @param {object} raw - Raw air quality response from Open-Meteo
 * @returns {object} Transformed air quality with current, hourly, and pollen data
 */
export function transformAirQuality(raw) {
  const pollenAvailable = hasPollenData(raw.hourly);

  const result = {
    current: {
      time: raw.current?.time,
      aqi: raw.current?.us_aqi,
      aqiCategory: getAqiCategory(raw.current?.us_aqi),
      pm25: raw.current?.pm2_5,
      pm10: raw.current?.pm10,
      carbonMonoxide: raw.current?.carbon_monoxide,
      nitrogenDioxide: raw.current?.nitrogen_dioxide,
      sulphurDioxide: raw.current?.sulphur_dioxide,
      ozone: raw.current?.ozone,
      uvIndex: raw.current?.uv_index,
      uvIndexClearSky: raw.current?.uv_index_clear_sky,
    },
    hourly: {
      time: raw.hourly?.time,
      aqi: raw.hourly?.us_aqi,
      pm25: raw.hourly?.pm2_5,
      pm10: raw.hourly?.pm10,
      ozone: raw.hourly?.ozone,
      uvIndex: raw.hourly?.uv_index,
    },
    pollen: pollenAvailable
      ? {
          available: true,
          hourly: {
            time: raw.hourly?.time,
            alderPollen: raw.hourly?.alder_pollen,
            birchPollen: raw.hourly?.birch_pollen,
            grassPollen: raw.hourly?.grass_pollen,
            mugwortPollen: raw.hourly?.mugwort_pollen,
            olivePollen: raw.hourly?.olive_pollen,
            ragweedPollen: raw.hourly?.ragweed_pollen,
          },
        }
      : { available: false, note: 'Pollen data only available in Europe' },
  };

  return result;
}

/**
 * Transform raw Open-Meteo geocoding results into the client-facing shape.
 * @param {object} raw - Raw geocoding response from Open-Meteo
 * @returns {{ results: object[] }}
 */
export function transformGeocodingResults(raw) {
  const results = (raw.results ?? []).map((r) => ({
    id: r.id,
    name: r.name,
    latitude: r.latitude,
    longitude: r.longitude,
    elevation: r.elevation,
    timezone: r.timezone,
    country: r.country,
    countryCode: r.country_code,
    admin1: r.admin1,
    admin2: r.admin2,
    population: r.population,
    featureCode: r.feature_code,
  }));

  return { results };
}

/**
 * Transform raw Open-Meteo historical archive data into the client-facing shape.
 * @param {object} raw - Raw historical response from Open-Meteo
 * @param {'metric' | 'imperial'} [units='metric']
 * @returns {object} Transformed historical data with location, daily, hourly, and units
 */
export function transformHistorical(raw, units = 'metric') {
  const conv = {
    temp: (v) => convertTemperature(v, units),
    speed: (v) => convertSpeed(v, units),
    precip: (v) => convertPrecipitation(v, units),
  };

  const dailyEnriched = enrichDailyWeatherCodes(raw.daily?.weather_code);

  const hourlyEnriched = enrichWeatherCodes(raw.hourly?.weather_code, null);

  return {
    location: {
      latitude: raw.latitude,
      longitude: raw.longitude,
      elevation: raw.elevation,
      timezone: raw.timezone,
      timezoneAbbreviation: raw.timezone_abbreviation,
      utcOffsetSeconds: raw.utc_offset_seconds,
    },

    daily: {
      time: raw.daily?.time,
      temperatureMax: convertArray(raw.daily?.temperature_2m_max, conv.temp),
      temperatureMin: convertArray(raw.daily?.temperature_2m_min, conv.temp),
      feelsLikeMax: convertArray(raw.daily?.apparent_temperature_max, conv.temp),
      feelsLikeMin: convertArray(raw.daily?.apparent_temperature_min, conv.temp),
      precipitationSum: convertArray(raw.daily?.precipitation_sum, conv.precip),
      rainSum: convertArray(raw.daily?.rain_sum, conv.precip),
      snowfallSum: convertArray(raw.daily?.snowfall_sum, conv.precip),
      weatherCode: raw.daily?.weather_code,
      weatherDescription: dailyEnriched.descriptions,
      weatherIcon: dailyEnriched.icons,
      sunrise: raw.daily?.sunrise,
      sunset: raw.daily?.sunset,
      sunshineDuration: raw.daily?.sunshine_duration,
      windSpeedMax: convertArray(raw.daily?.wind_speed_10m_max, conv.speed),
      windGustsMax: convertArray(raw.daily?.wind_gusts_10m_max, conv.speed),
      windDirectionDominant: raw.daily?.wind_direction_10m_dominant,
    },

    hourly: {
      time: raw.hourly?.time,
      temperature: convertArray(raw.hourly?.temperature_2m, conv.temp),
      feelsLike: convertArray(raw.hourly?.apparent_temperature, conv.temp),
      humidity: raw.hourly?.relative_humidity_2m,
      precipitation: convertArray(raw.hourly?.precipitation, conv.precip),
      rain: convertArray(raw.hourly?.rain, conv.precip),
      snowfall: convertArray(raw.hourly?.snowfall, conv.precip),
      weatherCode: raw.hourly?.weather_code,
      weatherDescription: hourlyEnriched.descriptions,
      weatherIcon: hourlyEnriched.icons,
      cloudCover: raw.hourly?.cloud_cover,
      windSpeed: convertArray(raw.hourly?.wind_speed_10m, conv.speed),
      windDirection: raw.hourly?.wind_direction_10m,
      pressureMsl: raw.hourly?.pressure_msl,
    },

    units: getUnitLabels(units),
  };
}

export default { transformForecast, transformAirQuality, transformGeocodingResults, transformHistorical };
