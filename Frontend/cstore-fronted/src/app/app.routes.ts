import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  // ── Públicas ─────────────────────────────────────────────────
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./pages/forgot-password/forgot-password.component').then(
        (m) => m.ForgotPasswordComponent
      ),
  },
  {
    path: 'unauthorized',
    loadComponent: () =>
      import('./pages/unauthorized/unauthorized.component').then(
        (m) => m.UnauthorizedComponent
      ),
  },

  // ── Solo ADMIN ───────────────────────────────────────────────
  {
    path: 'admin',
    loadComponent: () =>
      import('./pages/admin/admin.component').then((m) => m.AdminComponent),
    canActivate: [AuthGuard, AdminGuard],
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./pages/Dashboard/dashboard.component').then(
        (m: any) => m.DashboardComponent ?? m.default ?? m.Dashboard
      ),
    canActivate: [AuthGuard, AdminGuard],
  },
  {
    path: 'usuario',
    loadComponent: () =>
      import('./pages/usuario/usuario.component').then((m) => m.UsuarioComponent),
    canActivate: [AuthGuard, AdminGuard],
  },
  {
    path: 'signup',
    loadComponent: () =>
      import('./pages/signup/signup.component').then((m) => m.SignupComponent),
    canActivate: [AuthGuard, AdminGuard],
  },
  {
    path: 'ventas',
    loadComponent: () =>
      import('./pages/ventas/ventas.component').then((m) => m.VentasComponent),
    canActivate: [AuthGuard, AdminGuard],
  },

  {
    path: 'cliente',
    loadComponent: () =>
      import('./pages/Cliente/cliente.component').then((m) => m.ClienteComponent),
    canActivate: [AuthGuard, AdminGuard],
  },

  // ── Solo USUARIO (autenticado, cualquier rol) ────────────────
  {
    path: 'user-home',
    loadComponent: () =>
      import('./pages/user/user-home.component').then((m) => m.UserHomeComponent),
    canActivate: [AuthGuard],
  },
  {
    path: 'change-password',
    loadComponent: () =>
      import('./pages/change-password/change-password.component').then(
        (m) => m.ChangePasswordComponent
      ),
    canActivate: [AuthGuard],
  },
  {
    path: 'categoria',
    loadComponent: () =>
      import('./pages/categoria/categoria.component').then((m) => m.CategoriaComponent),
    canActivate: [AuthGuard],
  },
  {
    path: 'producto',
    loadComponent: () =>
      import('./pages/producto/producto.component').then((m) => m.ProductoComponent),
    canActivate: [AuthGuard],
  },
  {
    path: 'ordenes',
    loadComponent: () =>
      import('./pages/ordenes/ordenes.component').then((m) => m.BillComponent),
    canActivate: [AuthGuard],
  },
  {
    path: 'user',
    loadComponent: () =>
      import('./pages/user/user-home.component').then((m) => m.UserHomeComponent),
    canActivate: [AuthGuard],
  },

  // ── Fallback ─────────────────────────────────────────────────
  {
    path: '**',
    redirectTo: 'login',
  },
];
