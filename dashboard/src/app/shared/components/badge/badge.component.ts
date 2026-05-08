import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';

type BadgeVariant = 'severity-low' | 'severity-medium' | 'severity-high' | 'severity-critical'
  | 'status-authorized' | 'status-declined' | 'status-flagged'
  | 'type' | 'neutral';

@Component({
  selector: 'app-badge',
  standalone: true,
  imports: [NgClass],
  template: `<span class="badge" [ngClass]="variant">{{ label }}</span>`,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 2px 8px;
      border-radius: 5px;
      font-size: 0.65rem;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      font-family: var(--font-mono);
      white-space: nowrap;
    }

    .severity-low      { background: rgba(16, 185, 129, 0.1);  color: #10b981; border: 1px solid rgba(16, 185, 129, 0.2); }
    .severity-medium   { background: rgba(245, 158, 11, 0.1);  color: #f59e0b; border: 1px solid rgba(245, 158, 11, 0.2); }
    .severity-high     { background: rgba(244, 63, 94, 0.1);   color: #f43f5e; border: 1px solid rgba(244, 63, 94, 0.2); }
    .severity-critical { background: rgba(168, 85, 247, 0.12); color: #a855f7; border: 1px solid rgba(168, 85, 247, 0.25); }

    .status-authorized { background: rgba(16, 185, 129, 0.1); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.2); }
    .status-declined   { background: rgba(244, 63, 94, 0.1);  color: #f43f5e; border: 1px solid rgba(244, 63, 94, 0.2); }
    .status-flagged    { background: rgba(245, 158, 11, 0.1); color: #f59e0b; border: 1px solid rgba(245, 158, 11, 0.2); }

    .type    { background: rgba(6, 182, 212, 0.1); color: #06b6d4; border: 1px solid rgba(6, 182, 212, 0.2); }
    .neutral { background: rgba(255, 255, 255, 0.05); color: #8896b3; border: 1px solid rgba(255,255,255,0.08); }
  `,
})
export class BadgeComponent {
  @Input() label = '';
  @Input() variant: BadgeVariant = 'neutral';
}
