function renderFooter() {
  const container = document.getElementById("footer");
  if (!container) return;

  container.innerHTML = `<footer class="footer">
    <div class="footer-container">
      <div class="footer-logo">
        <img src="/assets/images/logo/logo.png" alt="Smart Clinic logo">
        <p>&copy; ${new Date().getFullYear()} Smart Clinic</p>
      </div>
      <div class="footer-links">
        <div class="footer-column"><h4>Clinic</h4><a href="/">Home</a></div>
        <div class="footer-column"><h4>Patients</h4><a href="/pages/patientDashboard.html">Find a doctor</a></div>
      </div>
    </div>
  </footer>`;
}

renderFooter();
