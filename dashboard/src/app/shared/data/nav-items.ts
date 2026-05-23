import { INavData } from '@coreui/angular';

export const navItems: INavData[] = [
  {
    name: 'Overview',
    url: '/',
    iconComponent: { name: 'cilSpeedometer' },
    linkProps: { routerLinkActiveOptions: { exact: true } },
  },
  { title: true, name: 'Monitoring' },
  {
    name: 'Transactions',
    url: '/transactions',
    iconComponent: { name: 'cilTransfer' },
  },
  {
    name: 'Anomalies',
    url: '/anomalies',
    iconComponent: { name: 'cilWarning' },
  },
  {
    name: 'Reports',
    url: '/reports',
    iconComponent: { name: 'cilNotes' },
  },
  { title: true, name: 'System' },
  {
    name: 'Health',
    url: '/health',
    iconComponent: { name: 'cilHeart' },
  },
];
