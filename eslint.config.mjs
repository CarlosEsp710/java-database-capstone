export default [
  {
    files: ["app/src/main/resources/static/**/*.js"],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module"
    },
    rules: {
      "no-constant-condition": "error",
      "no-dupe-args": "error",
      "no-duplicate-case": "error",
      "no-empty": "error",
      "no-unreachable": "error",
      "no-unsafe-finally": "error"
    }
  }
];
