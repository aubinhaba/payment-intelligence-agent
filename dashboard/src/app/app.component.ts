import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { IconSetService } from '@coreui/icons-angular';
import {
  cilSpeedometer,
  cilTransfer,
  cilWarning,
  cilNotes,
  cilHeart,
  cilMenu,
  cilShieldAlt,
  cilChartLine,
  cilSearch,
  cilReload,
  cilArrowLeft,
  cilCopy,
  cilCheckCircle,
  cilLockLocked,
  cilLockUnlocked,
  cilAccountLogout,
  cilBell,
} from '@coreui/icons';
import { ThemeService } from './shared/services/theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet />`,
})
export class AppComponent {
  private readonly iconSetService = inject(IconSetService);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  private readonly themeService = inject(ThemeService);

  constructor() {
    this.iconSetService.icons = {
      cilSpeedometer,
      cilTransfer,
      cilWarning,
      cilNotes,
      cilHeart,
      cilMenu,
      cilShieldAlt,
      cilChartLine,
      cilSearch,
      cilReload,
      cilArrowLeft,
      cilCopy,
      cilCheckCircle,
      cilLockLocked,
      cilLockUnlocked,
      cilAccountLogout,
      cilBell,
    };
  }
}
