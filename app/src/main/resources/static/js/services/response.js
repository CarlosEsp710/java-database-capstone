export async function readJson(response) {
  let data;
  try {
    data = response.status === 204 ? {} : await response.json();
  } catch (error) {
    if (!response.ok) throw new Error(`Request failed (${response.status}).`, { cause: error });
    throw new Error(`Invalid response from server (${response.status}).`, { cause: error });
  }

  if (!response.ok) {
    throw new Error(data.message || `Request failed (${response.status}).`);
  }
  return data;
}
