import { InjectionToken } from '@angular/core';
import { Configuration } from '../generated/api';
import { environment } from 'src/environments/environment';

export const API_CONFIG_TOKEN = new InjectionToken<Configuration>('API_CONFIG');

export function createApiConfiguration(): Configuration {
  return new Configuration({
    basePath: environment.apiUrl,
    // Si necesitas autenticación
    credentials: {
      bearer: () => localStorage.getItem('token') || ''
    }
  });
}