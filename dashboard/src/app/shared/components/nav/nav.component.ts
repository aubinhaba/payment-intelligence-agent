import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="sidebar">
      <div class="logo">
        <div class="logo-mark">
          <span class="logo-diamond">◈</span>
        </div>
        <div class="logo-text">
          <span class="logo-name">PIA</span>
          <span class="logo-sub">Intelligence Agent</span>
        </div>
      </div>

      <div class="nav-section-label">Navigation</div>

      <ul class="nav-list">
        @for (item of navItems; track item.route) {
          <li>
            <a
              [routerLink]="item.route"
              routerLinkActive="active"
              [routerLinkActiveOptions]="{ exact: item.route === '/' }"
              class="nav-link"
            >
              <span class="nav-icon">{{ item.icon }}</span>
              <span class="nav-label">{{ item.label }}</span>
              <span class="nav-arrow">›</span>
            </a>
          </li>
        }
      </ul>

      <div class="sidebar-bottom">
        <div class="status-row">
          <span class="status-dot"></span>
          <span class="status-text">System live</span>
        </div>
        <span class="version">v1.0.0-alpha</span>
      </div>
    </nav>
  `,
  styles: `
    .sidebar {
      width: 232px;
      min-height: 100vh;
      background: rgba(9, 14, 28, 0.95);
      border-right: 1px solid rgba(99, 102, 241, 0.1);
      display: flex;
      flex-direction: column;
      padding: 1.5rem 0 1.25rem;
      position: sticky;
      top: 0;
      backdrop-filter: blur(20px);
      flex-shrink: 0;
    }

    /* Logo */
    .logo {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0 1.25rem 1.5rem;
      border-bottom: 1px solid rgba(99, 102, 241, 0.08);
      margin-bottom: 1.25rem;
    }

    .logo-mark {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: linear-gradient(135deg, #6366f1 0%, #06b6d4 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 0 16px rgba(99, 102, 241, 0.4);
      flex-shrink: 0;
    }

    .logo-diamond {
      color: #fff;
      font-size: 1.1rem;
      line-height: 1;
    }

    .logo-text {
      display: flex;
      flex-direction: column;
      gap: 1px;
    }

    .logo-name {
      font-size: 0.9rem;
      font-weight: 800;
      letter-spacing: 0.1em;
      text-transform: uppercase;
      background: linear-gradient(135deg, #c7d2fe, #a5f3fc);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .logo-sub {
      font-size: 0.6rem;
      color: #3d4d6b;
      letter-spacing: 0.04em;
      font-weight: 500;
    }

    /* Section label */
    .nav-section-label {
      font-size: 0.6rem;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: #3d4d6b;
      padding: 0 1.25rem 0.5rem;
    }

    /* Nav list */
    .nav-list {
      list-style: none;
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 2px;
      padding: 0 0.625rem;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 0.6rem 0.75rem;
      border-radius: 10px;
      color: #8896b3;
      text-decoration: none;
      font-size: 0.825rem;
      font-weight: 500;
      transition: color var(--transition-fast), background var(--transition-fast);
      position: relative;

      .nav-arrow {
        margin-left: auto;
        opacity: 0;
        font-size: 0.75rem;
        transition: opacity var(--transition-fast), transform var(--transition-fast);
      }

      &:hover {
        color: #eef2ff;
        background: rgba(99, 102, 241, 0.07);

        .nav-arrow { opacity: 0.4; transform: translateX(2px); }
      }

      &.active {
        color: #eef2ff;
        background: rgba(99, 102, 241, 0.12);

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 25%;
          height: 50%;
          width: 3px;
          border-radius: 0 2px 2px 0;
          background: linear-gradient(180deg, #6366f1, #06b6d4);
        }

        .nav-arrow { opacity: 0.7; transform: translateX(2px); }
      }
    }

    .nav-icon {
      font-size: 0.9rem;
      width: 1.125rem;
      text-align: center;
      flex-shrink: 0;
    }

    /* Bottom */
    .sidebar-bottom {
      padding: 1rem 1.25rem 0;
      border-top: 1px solid rgba(99, 102, 241, 0.08);
      margin-top: 1rem;
      display: flex;
      flex-direction: column;
      gap: 0.375rem;
    }

    .status-row {
      display: flex;
      align-items: center;
      gap: 0.375rem;
    }

    .status-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #10b981;
      box-shadow: 0 0 6px #10b981;
      animation: pulse 2.5s ease-in-out infinite;
    }

    .status-text {
      font-size: 0.7rem;
      color: #10b981;
      font-weight: 500;
    }

    .version {
      font-size: 0.625rem;
      color: #3d4d6b;
      font-family: var(--font-mono);
    }

    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50%       { opacity: 0.5; }
    }
  `,
})
export class NavComponent {
  readonly navItems: NavItem[] = [
    { label: 'Overview',     route: '/',             icon: '▦' },
    { label: 'Transactions', route: '/transactions',  icon: '⇄' },
    { label: 'Anomalies',    route: '/anomalies',     icon: '⚑' },
    { label: 'Reports',      route: '/reports',       icon: '≡' },
    { label: 'System Health', route: '/health',       icon: '◎' },
  ];
}
