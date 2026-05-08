import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavComponent } from './shared/components/nav/nav.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavComponent],
  template: `
    <div class="shell">
      <app-nav />
      <main class="main-content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: `
    .shell {
      display: flex;
      min-height: 100vh;
    }

    .main-content {
      flex: 1;
      overflow-y: auto;
      background: var(--color-bg);
    }
  `,
})
export class AppComponent {}
