import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ICONS } from '../../icons';

interface NavItem {
  label: string;
  route: string;
  exact: boolean;
  safeIcon: SafeHtml;
}

@Component({
  selector: 'app-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="sidebar">
      <div class="logo">
        <div class="logo-mark" [innerHTML]="logoIcon"></div>
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
              [routerLinkActiveOptions]="{ exact: item.exact }"
              class="nav-link"
            >
              <span class="nav-icon" [innerHTML]="item.safeIcon"></span>
              <span class="nav-label">{{ item.label }}</span>
              <span class="nav-chevron" [innerHTML]="chevronIcon"></span>
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
    :host { display: contents; }

    .sidebar {
      width: 220px;
      min-height: 100vh;
      background: rgba(9, 14, 28, 0.97);
      border-right: 1px solid rgba(99, 102, 241, 0.09);
      display: flex;
      flex-direction: column;
      padding: 1.25rem 0 1rem;
      position: sticky;
      top: 0;
      height: 100vh;
      backdrop-filter: blur(20px);
      flex-shrink: 0;
      overflow-y: auto;
      overflow-x: hidden;
    }

    /* Logo */
    .logo {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 0 1rem 1.25rem;
      border-bottom: 1px solid rgba(99, 102, 241, 0.08);
      margin-bottom: 1rem;
    }

    .logo-mark {
      width: 32px;
      height: 32px;
      border-radius: 8px;
      background: linear-gradient(135deg, #6366f1 0%, #06b6d4 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 0 14px rgba(99, 102, 241, 0.4);
      flex-shrink: 0;
      color: #fff;

      ::ng-deep svg {
        width: 16px;
        height: 16px;
        stroke: #fff;
        stroke-width: 2.5;
      }
    }

    .logo-text {
      display: flex;
      flex-direction: column;
      gap: 1px;
      min-width: 0;
    }

    .logo-name {
      font-size: 0.85rem;
      font-weight: 800;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      background: linear-gradient(135deg, #c7d2fe, #a5f3fc);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .logo-sub {
      font-size: 0.58rem;
      color: #3d4d6b;
      letter-spacing: 0.03em;
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    /* Section label */
    .nav-section-label {
      font-size: 0.58rem;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: #3d4d6b;
      padding: 0 1rem 0.5rem;
    }

    /* Nav list */
    .nav-list {
      list-style: none;
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 1px;
      padding: 0 0.5rem;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 0.55rem 0.75rem;
      border-radius: 8px;
      color: var(--color-text-secondary);
      text-decoration: none;
      font-size: 0.8125rem;
      font-weight: 500;
      transition: color var(--transition-fast), background var(--transition-fast);
      position: relative;
      min-width: 0;

      &:hover {
        color: var(--color-text-primary);
        background: rgba(99, 102, 241, 0.07);

        .nav-chevron { opacity: 0.5; transform: translateX(2px); }
      }

      &.active {
        color: var(--color-text-primary);
        background: rgba(99, 102, 241, 0.11);

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 20%;
          height: 60%;
          width: 3px;
          border-radius: 0 2px 2px 0;
          background: linear-gradient(180deg, #6366f1, #06b6d4);
        }

        .nav-chevron { opacity: 0.6; transform: translateX(2px); }
      }
    }

    .nav-icon {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0.75;
      transition: opacity var(--transition-fast);

      ::ng-deep svg {
        width: 15px;
        height: 15px;
      }
    }

    .nav-link:hover .nav-icon,
    .nav-link.active .nav-icon { opacity: 1; }

    .nav-label {
      flex: 1;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .nav-chevron {
      width: 14px;
      height: 14px;
      flex-shrink: 0;
      opacity: 0;
      transition: opacity var(--transition-fast), transform var(--transition-fast);
      color: var(--color-text-muted);

      ::ng-deep svg { width: 14px; height: 14px; }
    }

    /* Bottom */
    .sidebar-bottom {
      padding: 0.875rem 1rem 0;
      border-top: 1px solid rgba(99, 102, 241, 0.07);
      margin-top: 0.75rem;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
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
      animation: pulse-dot 2.5s ease-in-out infinite;
      flex-shrink: 0;
    }

    .status-text {
      font-size: 0.68rem;
      color: #10b981;
      font-weight: 500;
    }

    .version {
      font-size: 0.6rem;
      color: #3d4d6b;
      font-family: var(--font-mono);
    }

    @keyframes pulse-dot {
      0%, 100% { opacity: 1; }
      50%       { opacity: 0.45; }
    }

    /* ── Mobile: bottom tab bar ─────────────────────────────────── */
    @media (max-width: 700px) {
      .sidebar {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        width: 100%;
        min-height: unset;
        height: 56px;
        flex-direction: row;
        padding: 0;
        border-right: none;
        border-top: 1px solid rgba(99, 102, 241, 0.12);
        z-index: 100;
        overflow: visible;
        align-items: center;
        background: rgba(5, 8, 20, 0.97);
      }

      .logo,
      .nav-section-label,
      .sidebar-bottom { display: none; }

      .nav-list {
        flex-direction: row;
        justify-content: space-around;
        align-items: center;
        padding: 0;
        gap: 0;
        flex: 1;
      }

      li { flex: 1; }

      .nav-link {
        flex-direction: column;
        gap: 3px;
        padding: 0.5rem 0.25rem;
        border-radius: 0;
        justify-content: center;
        align-items: center;
        text-align: center;

        &::before { display: none !important; }

        &.active {
          background: transparent;

          .nav-icon { opacity: 1; color: var(--color-accent); }
          .nav-label { color: var(--color-accent); }
        }
      }

      .nav-icon {
        width: 18px;
        height: 18px;
        opacity: 0.6;
        ::ng-deep svg { width: 18px; height: 18px; }
      }

      .nav-label {
        font-size: 0.55rem;
        letter-spacing: 0.02em;
      }

      .nav-chevron { display: none; }
    }
  `,
})
export class NavComponent {
  private readonly sanitizer = inject(DomSanitizer);

  readonly logoIcon = this.sanitizer.bypassSecurityTrustHtml(ICONS.shield);
  readonly chevronIcon = this.sanitizer.bypassSecurityTrustHtml(ICONS.chevronRight);

  readonly navItems: NavItem[] = [
    { label: 'Overview',      route: '/',             exact: true,  safeIcon: this.sanitizer.bypassSecurityTrustHtml(ICONS.overview) },
    { label: 'Transactions',  route: '/transactions', exact: false, safeIcon: this.sanitizer.bypassSecurityTrustHtml(ICONS.transactions) },
    { label: 'Anomalies',     route: '/anomalies',    exact: false, safeIcon: this.sanitizer.bypassSecurityTrustHtml(ICONS.anomalies) },
    { label: 'Reports',       route: '/reports',      exact: false, safeIcon: this.sanitizer.bypassSecurityTrustHtml(ICONS.reports) },
    { label: 'System Health', route: '/health',       exact: false, safeIcon: this.sanitizer.bypassSecurityTrustHtml(ICONS.health) },
  ];
}
