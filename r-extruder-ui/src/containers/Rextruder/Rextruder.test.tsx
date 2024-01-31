import React from "react";
import { render } from "@testing-library/react";
import Rextruder from "./Rextruder";

test("should render Rextruder container", () => {
  const { getByText } = render(<Rextruder />);

  const uploadComponent = getByText("Select files...");
  expect(uploadComponent).toBeInTheDocument();

  const singleSelectComponent = getByText("Select parameters");
  expect(singleSelectComponent).toBeInTheDocument();
});
