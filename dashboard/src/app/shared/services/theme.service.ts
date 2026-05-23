import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  constructor() {
    document.documentElement.setAttribute('data-coreui-theme', 'light');
  }
}
