export default defineNuxtRouteMiddleware((to) => {
  const publicPages = ["/login", "/register"];
  const { isLoggedIn, initAuth, inited } = useAuth();

  if (import.meta.server) {
    return;
  }

  if (!inited.value) {
    initAuth();
  }

  if (!isLoggedIn.value && publicPages.includes(to.path)) {
    return;
  }

  if (!isLoggedIn.value) {
    return navigateTo("/login");
  }

  if (publicPages.includes(to.path)) {
    return navigateTo("/");
  }
});
